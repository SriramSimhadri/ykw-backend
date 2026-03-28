package com.ykw.common.filter;

import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logging filter used at each service to log the request response
 */
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            LogUtil.info(LogEvent.create("REQUEST_RECEIVED").path(request.getRequestURI()).method(request.getMethod()));
            filterChain.doFilter(request, response);
        } finally {

            long latency = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            LogUtil.info(LogEvent.create("REQUEST_COMPLETED").status(status).latency(latency).path(request.getRequestURI()).method(request.getMethod()));
        }
    }
}