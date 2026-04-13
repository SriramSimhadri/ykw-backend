package com.ykw.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static com.ykw.common.constants.Constants.TRACE_ID;
import static com.ykw.common.constants.Constants.USER_ID;

public class LogUtil {

    private static final Logger log = LoggerFactory.getLogger("YKW_APP_LOGGER");

    private static Map<String, Object> enrich(Map<String, Object> fields) {
        Map<String, Object> enriched = new HashMap<>(fields);

        String traceId = MDC.get(TRACE_ID);
        String userId = MDC.get(USER_ID);

        if (traceId != null) enriched.put(TRACE_ID, traceId);
        if (userId != null) enriched.put(USER_ID, userId);

        return enriched;
    }

    private static void putAndLog(Map<String, Object> data, LogLevel level) {
        StringBuilder message = new StringBuilder();
        data.forEach((key, value) -> {
            if (value != null) {
                message.append(key)
                        .append("=")
                        .append(value)
                        .append(" ");
            }
        });
        String finalMessage = message.toString().trim();
        switch (level) {
            case INFO -> log.info(finalMessage);
            case WARN -> log.warn(finalMessage);
            case ERROR -> log.error(finalMessage);
            case DEBUG -> {
                if (log.isDebugEnabled()) {
                    log.debug(finalMessage);
                }
            }
        }
    }

    public static void info(LogEvent event) {
        putAndLog(enrich(event.build()), LogLevel.INFO);
    }

    public static void warn(LogEvent event) {
        putAndLog(enrich(event.build()), LogLevel.WARN);
    }

    public static void error(LogEvent event) {
        putAndLog(enrich(event.build()), LogLevel.ERROR);
    }

    public static void debug(LogEvent event) {
        putAndLog(enrich(event.build()), LogLevel.DEBUG);
    }

    private enum LogLevel {
        INFO, WARN, ERROR, DEBUG
    }
}