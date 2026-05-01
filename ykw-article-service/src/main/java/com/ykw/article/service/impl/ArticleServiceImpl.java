package com.ykw.article.service.impl;

import com.ykw.article.dto.ArticleResponse;
import com.ykw.article.dto.CreateArticleRequest;
import com.ykw.article.dto.UpdateArticleRequest;
import com.ykw.article.error.ResourceConflictException;
import com.ykw.article.error.ResourceNotFoundException;
import com.ykw.article.error.UnauthorizedException;
import com.ykw.article.mapper.ArticleMapper;
import com.ykw.article.model.article.Article;
import com.ykw.article.model.article.ArticleStatus;
import com.ykw.article.model.idempotency.ArticleIdempotency;
import com.ykw.article.model.idempotency.CreationStatus;
import com.ykw.article.model.outbox.OutboxEventStatus;
import com.ykw.article.model.outbox.OutboxEventType;
import com.ykw.article.repository.ArticleIdempotencyRepository;
import com.ykw.article.repository.ArticleRepository;
import com.ykw.article.service.ArticleService;
import com.ykw.article.service.OutboxService;
import com.ykw.article.util.ArticleUtil;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.security.CurrentUserContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.ykw.common.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final CurrentUserContext currentUserContext;
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final ArticleIdempotencyRepository idempotencyRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public ArticleResponse createArticle(String idempotencyKey, CreateArticleRequest request) {

        Long authorId = getCurrentUserId();


        String slug = ArticleUtil.slugify(request.getTitle());
        String articleId = ArticleUtil.articleId();

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .add(ARTICLE_ID, articleId)
                .add(ARTICLE_STATUS, request.getStatus())
                .userId(authorId));

        ArticleIdempotency articleCreation;

        try {
            articleCreation = idempotencyRepository.save(
                    ArticleIdempotency.builder()
                            .authorId(authorId)
                            .idempotencyKey(idempotencyKey)
                            .creationStatus(CreationStatus.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            // data integrity failed by the author_id and idempotency key
            articleCreation = idempotencyRepository
                    .findByAuthorIdAndIdempotencyKey(authorId, idempotencyKey)
                    .orElseThrow(() -> new ResourceNotFoundException("Idempotency record missing"));

            if (articleCreation.isInProgress()) {
                LogUtil.warn(LogEvent.create("ARTICLE_CREATION_IN_PROGRESS")
                        .add(ARTICLE_SLUG, slug)
                        .add(ARTICLE_ID, articleId)
                        .add(ARTICLE_STATUS, request.getStatus())
                        .userId(authorId));
                throw new ResourceConflictException("Article creation in progress");
            }

            if (articleCreation.isCompleted()) {
                Article existing = articleRepository.findById(articleCreation.getArticleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
                LogUtil.info(LogEvent.create("ARTICLE_ALREADY_CREATED")
                        .add(ARTICLE_SLUG, existing.getSlug())
                        .add(ARTICLE_ID, existing.getId())
                        .add(ARTICLE_STATUS, request.getStatus())
                        .add(CREATED_AT, existing.getCreatedAt())
                        .userId(authorId));
                return articleMapper.toResponse(existing);
            }

            throw new IllegalStateException("Unknown idempotency state");
        }

        // Create article
        Article saved = articleRepository.save(buildArticle(request, authorId, slug, articleId));

        // Update idempotency, to maintain duplicate article creation
        articleCreation.setCreationStatus(CreationStatus.COMPLETED);
        articleCreation.setArticleId(saved.getId());
        idempotencyRepository.save(articleCreation);

        LogUtil.info(LogEvent.create("ARTICLE_CREATION_COMPLETED")
                .add(ARTICLE_ID, saved.getId())
                .add(ARTICLE_SLUG, saved.getSlug())
                .add(ARTICLE_STATUS, request.getStatus())
                .add(CREATED_AT, saved.getCreatedAt())
                .userId(authorId));

        // outbox service will save the created record to the
        // database which will be later processed by the scheduler to send it to kafka
        outboxService.saveOutboxEvent(saved, OutboxEventType.ARTICLE_CREATED, OutboxEventStatus.NEW);

        return articleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(String idempotencyKey, String slug, UpdateArticleRequest request) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .add(ARTICLE_ID, request.getArticleId())
                .add(ARTICLE_STATUS, request.getStatus())
                .userId(authorId));

        ArticleIdempotency articleUpdate;
        try {
            articleUpdate = idempotencyRepository.save(
                    ArticleIdempotency.builder()
                            .authorId(authorId)
                            .idempotencyKey(idempotencyKey)
                            .creationStatus(CreationStatus.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException ex) {
            // data integrity failed by the author_id and idempotency key
            articleUpdate = idempotencyRepository
                    .findByAuthorIdAndIdempotencyKey(authorId, idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Idempotency record missing"));

            if (articleUpdate.isInProgress()) {
                LogUtil.warn(LogEvent.create("ARTICLE_UPDATE_IN_PROGRESS")
                        .add(ARTICLE_SLUG, slug)
                        .add(ARTICLE_ID, request.getArticleId())
                        .add(ARTICLE_STATUS, request.getStatus())
                        .userId(authorId));
                throw new ResourceConflictException("Article update in progress");
            }

            if (articleUpdate.isCompleted()) {
                Article existing = articleRepository.findById(articleUpdate.getArticleId())
                        .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
                LogUtil.info(LogEvent.create("ARTICLE_ALREADY_UPDATED")
                        .add(ARTICLE_SLUG, existing.getId())
                        .add(ARTICLE_ID, existing.getId())
                        .add(ARTICLE_STATUS, request.getStatus())
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

        articleUpdate.setCreationStatus(CreationStatus.COMPLETED);
        articleUpdate.setArticleId(article.getId());
        idempotencyRepository.save(articleUpdate);

        outboxService.saveOutboxEvent(article, OutboxEventType.ARTICLE_UPDATED, OutboxEventStatus.NEW);


        LogUtil.info(LogEvent.create("ARTICLE_UPDATE_COMPLETED")
                .add(ARTICLE_SLUG, article.getSlug())
                .add(ARTICLE_ID, article.getId())
                .add(ARTICLE_STATUS, request.getStatus())
                .add(UPDATED_AT, article.getUpdatedAt())
                .userId(article.getAuthorId()));

        return articleMapper.toResponse(article);
    }

    @Override
    @Transactional
    public void deleteArticle(String slug) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_DELETION_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .add(ARTICLE_STATUS, ArticleStatus.DELETED)
                .userId(authorId));

        Article article = articleRepository
                .findByAuthorIdAndSlugAndStatusNot(authorId, slug, ArticleStatus.DELETED)
                .orElseThrow(() -> {
                    LogUtil.error(LogEvent.create("ARTICLE_DELETED_NOT_FOUND")
                            .add(ARTICLE_SLUG, slug)
                            .userId(authorId));
                    return new ResourceNotFoundException("Article not found");
                });

        article.setStatus(ArticleStatus.DELETED);

        //save the deleted article to outbox event
        outboxService.saveOutboxEvent(article, OutboxEventType.ARTICLE_DELETED, OutboxEventStatus.NEW);

        LogUtil.info(LogEvent.create("ARTICLE_MARKED_AS_DELETED")
                .add(ARTICLE_SLUG, slug)
                .add(ARTICLE_STATUS, ArticleStatus.DELETED)
                .userId(authorId));
    }


    @Transactional
    @Override
    public ArticleResponse getArticleBySlug(String slug) {

        Long authorId = getCurrentUserId();

        LogUtil.info(LogEvent.create("ARTICLE_GET_INITIATED")
                .add(ARTICLE_SLUG, slug)
                .userId(authorId));







        LogUtil.info(LogEvent.create("ARTICLE_GET_COMPLETED")
                .add(ARTICLE_SLUG, slug)
                .userId(authorId));

        return null;

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

    @Scheduled(fixedDelay = 60 * 60 * 1000) //every one hour
    @Transactional
    public void cleanupIdempotency() {
        Instant cutoffCompleted = Instant.now().minus(Duration.ofHours(1));
        Instant cutoffInProgress = Instant.now().minus(Duration.ofHours(10));
        int deleted = idempotencyRepository.deleteOldRecords(cutoffCompleted, cutoffInProgress);
        LogUtil.info(LogEvent.create("IDEMPOTENCY_CLEANUP")
                .add(ARTICLE_IDEMPOTENCY_DELETED_COUNT, deleted));
    }
}