package com.ykw.api.gateway.health;

import com.ykw.api.gateway.config.GatewayHealthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HealthPoller periodically checks the health of downstream microservices
 * behind the API Gateway. It invokes the {@code /actuator/health} endpoint of each
 * registered service and stores the latest health status in memory.
 */
@Component
@RequiredArgsConstructor
public class HealthPoller {

    /**
     * Reactive HTTP client used to call service health endpoints.
     */
    private final WebClient webClient = WebClient.builder().build();

    /**
     * Thread-safe cache storing the latest health status of each service.
     */
    private final Map<String, Boolean> serviceStatus = new ConcurrentHashMap<>();

    /**
     * Holds the properties of the downstream services and time interval
     */
    private final GatewayHealthProperties properties;

    /**
     * Returns the latest cached health status of all services.
     *
     * @return map containing service health states
     */
    public Map<String, Boolean> getStatuses() {
        return serviceStatus;
    }

    /**
     * Periodically checks the health of all downstream services.
     * Runs every 5 seconds.
     */
    @Scheduled(fixedDelayString = "${gateway.health.interval:5000}")
    public void checkServices() {
        properties.getServices().forEach(this::check);
    }

    /**
     * Initiates an asynchronous health check for a given service.
     *
     * @param name service identifier
     * @param url  service base URL
     */
    private void check(String name, String url) {
        checkHealth(url)
                .subscribe(status -> serviceStatus.put(name, status));
    }

    /**
     * Calls the /actuator/health endpoint of the given service.
     *
     * @param serviceUrl base URL of the service
     * @return Mono<Boolean> representing service health status
     */
    public Mono<Boolean> checkHealth(String serviceUrl) {
        return webClient.get()
                .uri(serviceUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(r -> true)
                .onErrorReturn(false);
    }
}