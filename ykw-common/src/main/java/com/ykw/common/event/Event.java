package com.ykw.common.event;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event<T> {

    private String eventId;

    private String eventType;

    private String eventVersion; // v1, v2

    private String entity;

    private T payload;
}

