package com.ykw.article.service;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
import com.ykw.article.dto.UpdateArticleRequest;
import com.ykw.article.error.ResourceConflictException;
import com.ykw.article.error.ResourceNotFoundException;
import com.ykw.article.error.UnauthorizedException;
import com.ykw.article.mapper.ArticleMapper;
import com.ykw.article.model.*;
import com.ykw.article.repository.ArticleIdempotencyRepository;
import com.ykw.article.repository.ArticleRepository;
import com.ykw.article.util.ArticleUtil;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.security.CurrentUserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final String ARTICLE_ID = "article_id";
    private static final String ARTICLE_SLUG = "article_slug";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";

    private final CurrentUserContext currentUserContext;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final ArticleIdempotencyRepository idempotencyRepository;

    @Override
    @Transactional
    public ArticleResponse createArticle(String idempotencyKey, CreateArticleRequest request) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_INITIATED")
                .add(ARTICLE_SLUG, request.getTitle())
                .userId(authorId));

        String slug = ArticleUtil.slugify(request.getTitle());
        String articleId = ArticleUtil.articleId();

        ArticleIdempotency idempotency;

        try {
            idempotency = idempotencyRepository.save(
                    ArticleIdempotency.builder()
                            .authorId(authorId)
                            .idempotencyKey(idempotencyKey)
                            .creationStatus(CreationStatus.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            // data integrity failed by the author_id and idempotency key
            idempotency = idempotencyRepository
                    .findByAuthorIdAndIdempotencyKey(authorId, idempotencyKey)
                    .orElseThrow(() -> new ResourceNotFoundException("Idempotency record missing"));

            if (idempotency.isInProgress()) {
                LogUtil.warn(LogEvent.create("ARTICLE_CREATION_IN_PROGRESS")
                        .add(ARTICLE_SLUG, slug)
                        .userId(authorId));
                throw new ResourceConflictException("Article creation in progress");
            }

            if (idempotency.isCompleted()) {
                Article existing = articleRepository.findById(idempotency.getArticleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
                LogUtil.info(LogEvent.create("ARTICLE_ALREADY_CREATED")
                        .add(ARTICLE_ID, existing.getId())
                        .add(CREATED_AT, existing.getCreatedAt())
                        .userId(authorId));
                return articleMapper.toResponse(existing);
            }

            throw new IllegalStateException("Unknown idempotency state");
        }

        // Create article
        Article saved = articleRepository.save(buildArticle(request, authorId, slug, articleId));

        // Update idempotency
        idempotency.setCreationStatus(CreationStatus.COMPLETED);
        idempotency.setArticleId(saved.getId());
        idempotencyRepository.save(idempotency);

        // TODO: Outbox event

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_COMPLETED")
                .add(ARTICLE_ID, saved.getId())
                .add(ARTICLE_SLUG, saved.getSlug())
                .add(CREATED_AT, saved.getCreatedAt())
                .userId(authorId));

        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(String idempotencyKey, String slug, UpdateArticleRequest request) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .add(ARTICLE_ID, request.getArticleId())
                .userId(authorId));

        ArticleIdempotency idempotency;
        try {
            idempotency = idempotencyRepository.save(
                    ArticleIdempotency.builder()
                            .authorId(authorId)
                            .idempotencyKey(idempotencyKey)
                            .creationStatus(CreationStatus.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            // data integrity failed by the author_id and idempotency key
            idempotency = idempotencyRepository
                    .findByAuthorIdAndIdempotencyKey(authorId, idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency record missing"));

            if (idempotency.isInProgress()) {
                LogUtil.warn(LogEvent.create("ARTICLE_UPDATE_IN_PROGRESS")
                        .add(ARTICLE_SLUG, slug)
                        .userId(authorId));
                throw new ResourceConflictException("Article update in progress");
            }

            if (idempotency.isCompleted()) {
                Article existing = articleRepository.findById(idempotency.getArticleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
                LogUtil.info(LogEvent.create("ARTICLE_ALREADY_UPDATED")
                        .add(ARTICLE_ID, existing.getId())
                        .add(UPDATED_AT, existing.getUpdatedAt())
                        .userId(authorId));
                return articleMapper.toResponse(existing);
            }

            throw new IllegalStateException("Unknown idempotency state");
        }

        Article article = articleRepository
                .findByAuthorIdAndSlugAndStatusNot(authorId, slug, ArticleStatus.DELETED)
                .orElseThrow(() -> {
                    LogUtil.error(LogEvent.create("ARTICLE_NOT_FOUND")
                            .add(ARTICLE_SLUG, slug)
                            .userId(authorId));
                    return new ResourceNotFoundException("Article not found");
                });

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setStatus(ArticleStatus.valueOf(request.getStatus().name()));
        article.setSubtitle(request.getSubtitle());
        article.setCoverImageUrl(request.getCoverImageUrl());

        idempotency.setCreationStatus(CreationStatus.COMPLETED);
        idempotency.setArticleId(article.getId());
        idempotencyRepository.save(idempotency);

        // TODO: Outbox event

        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_COMPLETED")
                .add(ARTICLE_ID, article.getId())
                .add(UPDATED_AT, article.getUpdatedAt())
                .userId(authorId));

        return articleMapper.toResponse(article);
    }

    @Override
    @Transactional
    public void deleteArticle(String slug) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .userId(authorId));

        long deleted = articleRepository.softDeleteBySlugAndAuthorId(slug, authorId);

        if (deleted == 0) {
            LogUtil.info(LogEvent.create("ARTICLE_DELETED_OR_NOT_FOUND")
                    .add(ARTICLE_SLUG, slug)
                    .userId(authorId));
            return;
        }

        // TODO: Outbox event

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_SUCCESS")
                .add(ARTICLE_SLUG, slug)
                .userId(authorId));
    }

    private Long getCurrentUserId() {
        return Optional.ofNullable(currentUserContext.getCurrentUser().userId())
                .orElseThrow(() -> new UnauthorizedException("Unauthorized user"));
    }

    private Article buildArticle(CreateArticleRequest request,
                                 Long authorId,
                                 String slug,
                                 String articleId) {

        return Article.builder()
                .id(articleId)
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .content(request.getContent())
                .authorId(authorId)
                .status(ArticleStatus.valueOf(request.getStatus().name()))
                .coverImageUrl(request.getCoverImageUrl())
                .slug(slug)
                .build();
    }
}