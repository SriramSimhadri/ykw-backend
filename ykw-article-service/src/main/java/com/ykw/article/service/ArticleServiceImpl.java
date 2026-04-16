package com.ykw.article.service;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
import com.ykw.article.error.ResourceConflictException;
import com.ykw.article.error.ResourceNotFoundException;
import com.ykw.article.error.UnauthorizedException;
import com.ykw.article.mapper.ArticleMapper;
import com.ykw.article.model.Article;
import com.ykw.article.model.ArticleStatus;
import com.ykw.article.repository.ArticleRepository;
import com.ykw.article.util.ArticleUtil;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.security.CurrentUserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.ykw.common.constants.Constants.ARTICLE_SLUG;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final CurrentUserContext currentUserContext;

    private final ArticleRepository articleRepository;

    private final ArticleMapper articleMapper;

    private final CacheService cacheService;

    @Override
    @Transactional
    public ArticleResponse createArticle(String idempotencyKey, CreateArticleRequest request) {

        Long userId = Optional.ofNullable(currentUserContext.getCurrentUser().userId())
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        String value = cacheService.getValue(userId, idempotencyKey);

        // Case 1: Already processed
        if (value != null && !cacheService.isLock(value)) {
            Article article = articleRepository.findById(value)
                    .orElseThrow(() -> new ResourceNotFoundException("Article not found"));

            return articleMapper.toResponse(article);
        }

        // Case 2: In progress -> request in progress
        if (value != null && cacheService.isLock(value)) {
            throw new ResourceConflictException("Article creation request in progress");
        }

        // Case 3: First request -> acquire lock
        boolean acquired = cacheService.acquireLock(userId, idempotencyKey);

        if (!acquired) {
            // Someone else just acquired -> say request in progress
            throw new ResourceConflictException("Article creation request in progress");
        }

        // process
        String slug = ArticleUtil.slugify(request.getTitle());
        Article saved = getSaved(request, userId, slug);

        // replace LOCK with result
        cacheService.saveResult(userId, idempotencyKey, saved.getId());

        return articleMapper.toResponse(saved);
    }

    public Article getSaved(CreateArticleRequest request, Long userId, String slug) {

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_INITIATING")
                .userId(userId)
                .add(ARTICLE_SLUG, slug)
        );

        Article article = Article.builder()
                .id(ArticleUtil.articleId())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .content(request.getContent())
                .authorId(userId)
                .status(ArticleStatus.valueOf(request.getStatus().name()))
                .coverImageUrl(request.getCoverImageUrl())
                .slug(slug)
                .build();

        Article saved = articleRepository.save(article);

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_COMPLETED")
                .userId(userId)
                .add(ARTICLE_SLUG, saved.getSlug()));

        return saved;
    }
}