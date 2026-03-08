package com.ykw.api.gateway.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
     * Poller responsible for periodically checking the health of downstream services
     */
    private final HealthPoller poller;

    /**
     * Computes the aggregated health status of the gateway based on the
     * health of all registered downstream services.
     *
     * @return a reactive {@link Mono} containing the aggregated {@link Health} result
     */
    @Override
    public Mono<Health> health() {

        boolean allUp = poller.getStatuses()
                .values()
                .stream()
                .allMatch(Boolean::booleanValue);

        Health.Builder builder = allUp ? Health.up() : Health.down();

        poller.getStatuses().forEach(builder::withDetail);

        return Mono.just(builder.build());
    }
}