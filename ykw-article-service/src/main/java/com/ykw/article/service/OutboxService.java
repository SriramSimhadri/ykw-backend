package com.ykw.article.service;

import com.ykw.article.model.article.Article;
import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventStatus;
import com.ykw.article.model.outbox.OutboxEventType;

import java.util.List;

public interface OutboxService {

    void saveOutboxEvent(Article article, OutboxEventType eventType, OutboxEventStatus status);

    List<OutboxEvent> fetchAndMarkProcessing(int limit);

    void markSent(OutboxEvent event);

    void markFailed(OutboxEvent event);
}