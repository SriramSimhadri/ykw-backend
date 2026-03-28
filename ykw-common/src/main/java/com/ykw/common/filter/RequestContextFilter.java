package com.ykw.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static com.ykw.common.constants.Constants.*;

/**
 * Request context filter to capture the userid, userrole and traceid and propagate to MDC for
 * logging.
 */
public class RequestContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String traceId = Optional.ofNullable(request.getHeader(TRACE_HEADER)).orElse(UUID.randomUUID().toString());

        String userId = request.getHeader(USER_HEADER);
        String userRoles = request.getHeader(ROLE_HEADER);

        try {
            MDC.put(TRACE_ID, traceId);

            if (userId != null && !userId.isBlank()) {
                MDC.put(USER_ID, userId);
            }

            if (userRoles != null && !userRoles.isBlank()) {
                MDC.put(USER_ROLE, userRoles);
            }

            response.setHeader(TRACE_HEADER, traceId);

            if (userId != null) {
                response.setHeader(USER_HEADER, userId);
            }

            if (userRoles != null) {
                response.setHeader(ROLE_HEADER, userRoles);
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(USER_ID);
            MDC.remove(USER_ROLE);
        }
    }
}