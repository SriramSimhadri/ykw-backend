package com.ykw.common.logging;

import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static com.ykw.common.constants.Constants.TRACE_ID;
import static com.ykw.common.constants.Constants.USER_ID;
import static com.ykw.common.constants.LogConstants.*;

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

    public static void info(LogEvent event) {
        log.info(EVENT, StructuredArguments.entries(enrich(event.build())));
    }

    public static void warn(LogEvent event) {
        log.warn(EVENT, StructuredArguments.entries(enrich(event.build())));
    }

    public static void error(LogEvent event) {
        log.error(EVENT, StructuredArguments.entries(enrich(event.build())));
    }

    public static void debug(LogEvent event) {
        if (log.isDebugEnabled()) {
            log.debug(EVENT, StructuredArguments.entries(enrich(event.build())));
        }
    }
}