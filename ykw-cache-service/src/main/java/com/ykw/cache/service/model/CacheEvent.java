package com.ykw.cache.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheEvent {

    private String key;
    private Object payload;
    private Operation operation;

    public enum Operation {
        UPSERT,
        DELETE
    }
}