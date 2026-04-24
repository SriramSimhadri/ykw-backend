package com.ykw.article.service;

import com.ykw.article.events.ArticleEventPayload;
import com.ykw.article.model.Article;
import com.ykw.common.events.BaseEvent;
import com.ykw.common.events.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "article-events";

    public void publishCreated(Article article) {
        kafkaTemplate.send(TOPIC, article.getId(), buildEvent(article, EventType.ARTICLE_CREATED));
    }

    public void publishUpdate(Article article) {
        kafkaTemplate.send(TOPIC, article.getId(), buildEvent(article, EventType.ARTICLE_UPDATED));
    }

    public void publishDeleted(Article article) {
        kafkaTemplate.send(TOPIC, article.getId(), buildEvent(article, EventType.ARTICLE_DELETED));
    }

    private BaseEvent<ArticleEventPayload> buildEvent(Article article, EventType type) {
        return BaseEvent.<ArticleEventPayload>builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type.name())
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