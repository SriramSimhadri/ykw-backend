package com.ykw.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent<T> {
    private String eventId;
    private String eventType;
 //   private String version;
    private String source;
    private Instant timestamp;
    private T payload;
}