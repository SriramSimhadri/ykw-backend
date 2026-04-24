package com.ykw.cache.service.model;

import lombok.Data;

@Data
public class ReceivedEvent {
    private String eventId;
    private String entity;
    private String aggregateId;
    private String eventType;
    private String payload;
}