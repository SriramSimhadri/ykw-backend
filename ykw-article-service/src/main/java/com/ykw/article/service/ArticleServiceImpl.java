package com.ykw.article.service;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
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
import org.springframework.data.redis.core.RedisTemplate;
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
    public ArticleResponse createArticle(String header, CreateArticleRequest request) {

        Long userId = Optional.ofNullable(currentUserContext.getCurrentUser().userId())
                .orElseThrow(() -> {
                    LogUtil.error(LogEvent.create("USER_NOT_AUTHENTICATED"));
                    return new UnauthorizedException("User not authenticated");
                });

        //cache the article
        cacheService.cacheArticle(userId, header);

        String slug = ArticleUtil.slugify(request.getTitle());

        Article saved = getSaved(request, userId, slug);

        return articleMapper.toResponse(saved);
    }

    public Article getSaved(CreateArticleRequest request, Long userId, String slug) {

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_INITIATING")
                .userId(userId)
                .add(ARTICLE_SLUG, slug)
        );

        Article article = Article.builder()
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