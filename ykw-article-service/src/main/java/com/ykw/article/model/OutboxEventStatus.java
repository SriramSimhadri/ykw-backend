package com.ykw.article.model;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    SENT,
    FAILED
}
