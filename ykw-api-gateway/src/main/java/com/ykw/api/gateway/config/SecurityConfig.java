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
                        .pathMatchers("/api/auth/logout").authenticated()
                        .pathMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh")
                        .permitAll()
                        .pathMatchers("/api/users/public/**").permitAll()
                        .pathMatchers("/api/users/**").authenticated()
                        .pathMatchers("/internal/**").denyAll()
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