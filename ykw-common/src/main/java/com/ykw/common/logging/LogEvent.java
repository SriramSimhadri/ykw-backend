package com.ykw.common.logging;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ykw.common.constants.Constants.TRACE_ID;
import static com.ykw.common.constants.Constants.USER_ID;
import static com.ykw.common.constants.LogConstants.*;

public class LogEvent {

    private final Map<String, Object> fields = new HashMap<>();

    private LogEvent(String event) {
        fields.put(EVENT, event);
        fields.put(TIMESTAMP, Instant.now().toString());
    }

    public static LogEvent create(String event) {
        return new LogEvent(event);
    }

    public LogEvent traceId(String traceId) {
        if (traceId != null) fields.put(TRACE_ID, traceId);
        return this;
    }

    public LogEvent userId(Object userId) {
        if (userId != null) fields.put(USER_ID, userId);
        return this;
    }

    public LogEvent path(String path) {
        if (path != null) fields.put(URI_PATH, path);
        return this;
    }

    public LogEvent method(String method) {
        if (method != null) fields.put(REQUEST_METHOD, method);
        return this;
    }

    public LogEvent status(Integer status) {
        if (status != null) fields.put(STATUS, status);
        return this;
    }

    public LogEvent latency(Long latencyMs) {
        if (latencyMs != null) fields.put(LATENCY_MS, latencyMs);
        return this;
    }

    public LogEvent error(String error) {
        if (error != null) fields.put(ERROR, error);
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