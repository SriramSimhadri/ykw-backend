package com.ykw.article.bo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublishEvent {

    private String eventId;

    private String aggregateId;

    private String eventType;

    private String payload;

    private String entity;
}
