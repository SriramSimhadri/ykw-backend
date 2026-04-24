package com.ykw.article.model.outbox;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    SENT,
    FAILED
}
