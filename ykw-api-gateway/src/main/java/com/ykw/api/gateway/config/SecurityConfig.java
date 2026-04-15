package com.ykw.api.gateway.config;

import com.ykw.api.gateway.filter.UserContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         CustomAuthConverter converter,
                                                         UserContextFilter userContextFilter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Auth Service
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh"
                        ).permitAll()
                        .pathMatchers("/api/auth/logout").authenticated()

                        // Profile Service
                        .pathMatchers("/api/users/public/**").permitAll()
                        .pathMatchers("/api/users/**").authenticated()

                        // Article Service
                        .pathMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/articles",
                                "/api/articles/*",
                                "/api/tags"
                        ).permitAll()
                        .pathMatchers("/api/articles/feed").authenticated()
                        .pathMatchers(org.springframework.http.HttpMethod.POST, "/api/articles").authenticated()
                        .pathMatchers(org.springframework.http.HttpMethod.PUT, "/api/articles/*").authenticated()
                        .pathMatchers(org.springframework.http.HttpMethod.DELETE, "/api/articles/*").authenticated()
                        .pathMatchers("/api/articles/*/tags/**").authenticated()

                        // Internal - Blocked
                        .pathMatchers("/internal/**").denyAll()

                        // Default authenticated
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(converter))

                        //It tells Spring Security: When authentication fails, this is how you respond to the client
                        .authenticationEntryPoint((exchange, ex) -> {
                            if (ex instanceof AuthenticationServiceException) {
                                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                            } else {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            }
                            return exchange.getResponse().setComplete();
                        })
                )
                .addFilterAfter(userContextFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}