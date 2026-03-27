package com.ykw.auth.error;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiError {

    private Instant timestamp;

    private String traceId;

    private String errorCode;

    private String message;

    private String path;
}