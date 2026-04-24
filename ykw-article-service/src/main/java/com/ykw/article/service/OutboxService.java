package com.ykw.article.service;

import com.ykw.article.model.article.Article;
import com.ykw.article.model.outbox.OutboxEventStatus;
import com.ykw.article.model.outbox.OutboxEventType;

public interface OutboxService {

    void saveOutboxEvent(Article article, OutboxEventType eventType, OutboxEventStatus status);
}
