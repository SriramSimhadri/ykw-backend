package com.ykw.common.logging;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LogEvent {

    private final Map<String, Object> fields = new HashMap<>();

    private LogEvent(String event) {
        fields.put("event", event);
        fields.put("timestamp", Instant.now().toString());
    }

    public static LogEvent create(String event) {
        return new LogEvent(event);
    }

    public LogEvent traceId(String traceId) {
        if (traceId != null) fields.put("traceId", traceId);
        return this;
    }

    public LogEvent userId(Object userId) {
        if (userId != null) fields.put("userId", userId);
        return this;
    }

    public LogEvent path(String path) {
        if (path != null) fields.put("path", path);
        return this;
    }

    public LogEvent method(String method) {
        if (method != null) fields.put("method", method);
        return this;
    }

    public LogEvent status(Integer status) {
        if (status != null) fields.put("status", status);
        return this;
    }

    public LogEvent latency(Long latencyMs) {
        if (latencyMs != null) fields.put("latencyMs", latencyMs);
        return this;
    }

    public LogEvent error(String error) {
        if (error != null) fields.put("error", error);
        return this;
    }

    public LogEvent add(String key, Object value) {
        if (key != null && value != null) {
            fields.put(key, value);
        }
        return this;
    }

    public Map<String, Object> build() {
        return Collections.unmodifiableMap(fields);
    }
}