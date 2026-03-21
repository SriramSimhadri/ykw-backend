package com.ykw.api.gateway.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FallbackResponse {
    private Instant timestamp;
    private String service;
    private int status;
    private String error;
    private String message;
    private String path;
}
