package com.ykw.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.article.events.ArticleEventPayload;
import com.ykw.article.model.Article;
import com.ykw.article.model.OutboxEvent;
import com.ykw.article.model.OutboxEventStatus;
import com.ykw.article.repository.OutboxRepository;
import com.ykw.common.events.BaseEvent;
import com.ykw.common.events.EventType;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.ykw.common.constants.Constants.ARTICLE_ID;
import static com.ykw.common.constants.Constants.ARTICLE_SLUG;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final ObjectMapper objectMapper;

    private final OutboxRepository repository;

    @Transactional
    public void saveOutboxEvent(Article article, EventType eventType, OutboxEventStatus status) {
        try {
            BaseEvent<ArticleEventPayload> event = buildEvent(article, eventType);

            repository.save(
                    OutboxEvent.builder()
                            .eventId(event.getEventId())
                            .aggregateId(article.getId())
                            .eventType(event.getEventType())
                            .payload(objectMapper.writeValueAsString(event))
                            .status(status)
                            .retries(0)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build()
            );
        } catch (Exception e) {
            LogUtil.error(LogEvent.create("OUTBOX_EVENT_SAVE_FAILED")
                    .add(ARTICLE_ID, article.getId())
                    .add(ARTICLE_SLUG, article.getSlug())
                    .userId(article.getAuthorId()));
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private BaseEvent<ArticleEventPayload> buildEvent(Article article, EventType eventType) {
        return BaseEvent.<ArticleEventPayload> builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType.name())
                .source("ykw-article-service")
                .timestamp(Instant.now())
                .payload(
                        ArticleEventPayload.builder()
                                .articleId(article.getId())
                                .slug(article.getSlug())
                                .authorId(article.getAuthorId())
                                .title(article.getTitle())
                                .status(article.getStatus().name())
                                .build()
                )
                .build();
    }
}