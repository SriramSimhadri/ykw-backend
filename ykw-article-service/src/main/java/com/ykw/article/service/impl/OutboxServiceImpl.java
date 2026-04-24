package com.ykw.article.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.article.model.article.Article;
import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventStatus;
import com.ykw.article.model.outbox.OutboxEventType;
import com.ykw.article.repository.OutboxRepository;
import com.ykw.article.service.OutboxService;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import com.ykw.common.payload.ArticleEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.ykw.common.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class OutboxServiceImpl implements OutboxService {

    private final ObjectMapper objectMapper;

    private final OutboxRepository repository;

    @Value("spring.application.name")
    private String service;

    @Transactional
    public void saveOutboxEvent(Article article, OutboxEventType eventType, OutboxEventStatus status) {
        try {
            LogUtil.info(LogEvent.create("ARTICLE_SAVE_TO_OUTBOX_EVENT_STARTED")
                    .add(EVENT_TYPE, eventType.name())
                            .add(ARTICLE_SLUG, article.getSlug())
                            .add(ARTICLE_ID, article.getId())
                            .add(CREATED_AT, article.getCreatedAt())
                            .add(UPDATED_AT, article.getUpdatedAt())
                            .add(OUTBOX_EVENT_STATUS, status.name())
                    .userId(article.getAuthorId()));

            ArticleEvent payload = buildPayload(article);

            repository.save(
                    OutboxEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateId(article.getId())
                            .eventType(eventType)
                            .payload(objectMapper.writeValueAsString(payload))
                            .status(status)
                            .retries(0)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build()
            );

            LogUtil.info(LogEvent.create("ARTICLE_SAVE_TO_OUTBOX_EVENT_COMPLETED")
                            .add(EVENT_TYPE, eventType.name())
                    .add(ARTICLE_SLUG, article.getSlug())
                    .add(ARTICLE_ID, article.getId())
                    .add(CREATED_AT, article.getCreatedAt())
                    .add(UPDATED_AT, article.getUpdatedAt())
                    .add(OUTBOX_EVENT_STATUS, status.name())
                    .userId(article.getAuthorId()));

        } catch (Exception e) {
            LogUtil.error(LogEvent.create("ARTICLE_SAVE_TO_OUTBOX_EVENT_FAILED")
                    .add(EVENT_TYPE, eventType.name())
                    .add(ARTICLE_SLUG, article.getSlug())
                    .add(ARTICLE_ID, article.getId())
                    .add(CREATED_AT, article.getCreatedAt())
                    .add(UPDATED_AT, article.getUpdatedAt())
                    .add(OUTBOX_EVENT_STATUS, status.name())
                    .userId(article.getAuthorId()));
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private ArticleEvent buildPayload(Article article) {
        return ArticleEvent.builder()
                .id(article.getId())
                .authorId(article.getAuthorId())
                .status(article.getStatus().name())
                .content(article.getContent())
                .coverImageUrl(article.getCoverImageUrl())
                .subtitle(article.getSubtitle())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .slug(article.getSlug())
                .authorId(article.getAuthorId())
                .title(article.getTitle())
                .build();
    }
}