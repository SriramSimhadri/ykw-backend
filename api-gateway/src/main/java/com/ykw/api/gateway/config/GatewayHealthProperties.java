package com.ykw.api.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration properties for gateway health monitoring.
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.health")
public class GatewayHealthProperties {

    /**
     * Map of service name to base URL.
     */
    private Map<String, String> services;

    /**
     * Polling interval in milliseconds.
     */
    private long interval;
}