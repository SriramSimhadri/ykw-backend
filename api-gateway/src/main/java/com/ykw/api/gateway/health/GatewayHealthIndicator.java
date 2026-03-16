package com.ykw.api.gateway.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.ykw.api.gateway.config.GatewayHealthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ykw.common.constants.GatewayConstants.STATUS_UP;

/**
 * GatewayHealthIndicator aggregates the health status of all downstream
 * microservices behind the API Gateway.
 *
 * <p>The overall gateway health is determined using the following rule:</p>
 *
 * <ul>
 *   <li>If all services are UP → Gateway status = UP</li>
 *   <li>If any service is DOWN → Gateway status = DOWN</li>
 * </ul>
 *
 */
@Component
@RequiredArgsConstructor
public class GatewayHealthIndicator implements ReactiveHealthIndicator {

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
     * Computes the aggregated health status of the gateway based on the
     * health of all registered downstream services.
     *
     * @return a reactive {@link Mono} containing the aggregated {@link Health} result
     */
    @Override
    public Mono<Health> health() {

        boolean allUp = serviceStatus
                .values()
                .stream()
                .allMatch(Boolean::booleanValue);

        Health.Builder builder = allUp ? Health.up() : Health.down();

        serviceStatus.forEach(builder::withDetail);

        return Mono.just(builder.build());
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
                .bodyToMono(JsonNode.class)
                .map(json -> STATUS_UP.equals(json.get("status").asText()))
                .onErrorReturn(false);
    }
}