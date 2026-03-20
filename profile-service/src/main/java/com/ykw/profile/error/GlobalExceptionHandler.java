package com.ykw.profile.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Captures the exceptions that occurs globally and translates to respective http response
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex,
                                            HttpServletRequest request) {
        log.error("Resource not found error", ex);
        return buildError(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND_ERROR",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex,
                                                HttpServletRequest request) {
        log.error("Unauthorized error", ex);
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED_ERROR",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseError(DataAccessException ex,
                                                        HttpServletRequest request) {

        log.error("Database error", ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATABASE_ERROR",
                "Unexpected database error",
                request
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex,
                                                  HttpServletRequest request) {

        log.error("Runtime error", ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                ex.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status,
                                                String code,
                                                String message,
                                                HttpServletRequest request) {

        String traceId = MDC.get("traceId");

        ApiError error = ApiError.builder()
                .timestamp(Instant.now())
                .traceId(traceId)
                .errorCode(code)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
