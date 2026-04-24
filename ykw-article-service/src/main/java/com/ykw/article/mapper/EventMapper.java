package com.ykw.article.mapper;

import com.ykw.article.bo.PublishEvent;
import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "entity", constant = "article")
    PublishEvent toEvent(OutboxEvent outboxEvent);

    default String mapEventType(OutboxEventType eventType) {
        return eventType.name();
    }

}
