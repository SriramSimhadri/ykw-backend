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
import com.ykw.common.security.CurrentUserContext;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            Article article = getArticleById(value);
            return articleMapper.toResponse(article);
        }

        // Case 2: In progress
        if (value != null && cacheService.isLock(value)) {
            throw new ResourceConflictException("Article creation request in progress");
        }

        // Case 3: Acquire lock
        boolean acquired = cacheService.acquireLock(userId, idempotencyKey);

        if (!acquired) {
            throw new ResourceConflictException("Article creation request in progress");
        }

        // Process
        String slug = ArticleUtil.slugify(request.getTitle());
        Article saved = saveArticle(buildArticle(request, userId, slug));

        cacheService.saveResult(userId, idempotencyKey, saved.getId());

        return articleMapper.toResponse(saved);
    }

    @CircuitBreaker(name = "db", fallbackMethod = "saveFallback")
    public Article saveArticle(Article article) {
        return articleRepository.save(article);
    }

    @CircuitBreaker(name = "db", fallbackMethod = "getFallback")
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

    public Article saveFallback(Article article, Exception ex) {
        throw new RuntimeException("Database temporarily unavailable");
    }

    public Article getFallback(String id, Exception ex) {
        throw new RuntimeException("Database temporarily unavailable");
    }
}