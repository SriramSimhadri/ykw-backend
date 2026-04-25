package com.ykw.common.event;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event<T> {

    private String eventId;

    private String aggregateId;

    private String eventType;

    private String eventVersion;

    private String entity;

    private T payload;
}