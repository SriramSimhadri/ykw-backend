package com.ykw.common.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class LogUtil {

    private static final Logger log = LoggerFactory.getLogger("YKW_APP_LOGGER");

    private static Map<String, Object> enrich(Map<String, Object> fields) {

        Map<String, Object> enriched = new HashMap<>(fields);

        String traceId = MDC.get("traceId");
        String userId = MDC.get("userId");

        if (traceId != null) enriched.put("traceId", traceId);
        if (userId != null) enriched.put("userId", userId);

        return enriched;
    }

    public static void info(LogEvent event) {
        log.info("event", StructuredArguments.entries(enrich(event.build())));
    }

    public static void warn(LogEvent event) {
        log.warn("event", StructuredArguments.entries(enrich(event.build())));
    }

    public static void error(LogEvent event) {
        log.error("event", StructuredArguments.entries(enrich(event.build())));
    }

    public static void debug(LogEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("event", StructuredArguments.entries(enrich(event.build())));
        }
    }
}