package com.ykw.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class RequestContextFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";

    private static final String TRACE_HEADER = "X-Request-ID";
    private static final String USER_HEADER = "X-User-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // extract traceId
            String traceId = Optional.ofNullable(request.getHeader(TRACE_HEADER))
                    .orElse(UUID.randomUUID().toString());

            // extract user id
            String userId = request.getHeader(USER_HEADER);

            // map it to MDC
            MDC.put(TRACE_ID, traceId);
            if (userId != null) {
                MDC.put(USER_ID, userId);
            }

            // Continue request
            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}