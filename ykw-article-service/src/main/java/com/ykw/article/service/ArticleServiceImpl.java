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

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String ARTICLE_ID = "article_id";
    private static final String CREATED_AT = "created_at";
    private static final String ARTICLE_SLUG = "article_slug";

    private final CurrentUserContext currentUserContext;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public ArticleResponse createArticle(String idempotencyKey, CreateArticleRequest request) {

        Long userId = Optional.ofNullable(currentUserContext.getCurrentUser()
                        .userId()).orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_INITIATED").userId(userId));

        String slug = ArticleUtil.slugify(request.getTitle());
        String value = cacheService.getValue(userId, idempotencyKey);

        // Case 1: Already processed
        if (value != null && !cacheService.isLock(value)) {

            Article article = getArticleById(value);

            LogUtil.info(LogEvent.create("ARTICLE_ALREADY_CREATED")
                    .add(ARTICLE_ID, article.getId())
                    .add(CREATED_AT, article.getCreatedAt())
                    .userId(userId));

            return articleMapper.toResponse(article);
        }

        // Case 2: In progress
        if (value != null && cacheService.isLock(value)) {

            LogUtil.info(LogEvent.create("ARTICLE_CREATION_IN_PROGRESS")
                    .add(ARTICLE_SLUG, slug)
                    .userId(userId));
            throw new ResourceConflictException("Article creation request in progress");
        }

        // Case 3: Acquire lock
        boolean acquired = cacheService.acquireLock(userId, idempotencyKey);

        if (!acquired) {
            LogUtil.info(LogEvent.create("ARTICLE_CREATION_IN_PROGRESS")
                    .add(ARTICLE_SLUG, slug)
                    .userId(userId));
            throw new ResourceConflictException("Article creation request in progress");
        }

        // Process
        Article saved = articleRepository.save(buildArticle(request, userId, slug));

        cacheService.saveArticleMetadata(userId, saved.getSlug());

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_SUCCESS")
                .add(ARTICLE_ID, saved.getId())
                .add(ARTICLE_SLUG, saved.getSlug())
                .add(CREATED_AT, saved.getCreatedAt())
                .userId(userId));

        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteArticle(String slug) {

        Long userId = Optional.ofNullable(currentUserContext.getCurrentUser().userId())
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .userId(userId));

        long deleted = articleRepository.softDeleteBySlugAndAuthorId(slug, userId);

        if (deleted == 0) {
            LogUtil.info(LogEvent.create("ARTICLE_DELETED_OR_NOT_FOUND")
                    .add(ARTICLE_SLUG, slug)
                    .userId(userId));
            return;
        }

        cacheService.evictArticle(userId, slug);

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_SUCCESS")
                .add(ARTICLE_SLUG, slug)
                .userId(userId));
    }

    public Article getArticleById(String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private Article buildArticle(CreateArticleRequest request, Long userId, String slug) {
        return Article.builder()
                .id(ArticleUtil.articleId())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .content(request.getContent())
                .authorId(userId)
                .status(ArticleStatus.valueOf(request.getStatus().name()))
                .coverImageUrl(request.getCoverImageUrl())
                .slug(slug)
                .build();
    }
}