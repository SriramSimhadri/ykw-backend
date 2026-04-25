package com.ykw.article.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventType;
import com.ykw.common.event.ArticleEvent;
import com.ykw.common.event.Event;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "entity", constant = "article")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "payload", source = "payload")
    @Mapping(target = "eventVersion", constant = "v1")
    public abstract Event<ArticleEvent> toEvent(OutboxEvent outboxEvent);

    protected ArticleEvent map(JsonNode payload) {
        return objectMapper.convertValue(payload, ArticleEvent.class);
    }

    protected String map(OutboxEventType eventType) {
        return eventType.name();
    }
}