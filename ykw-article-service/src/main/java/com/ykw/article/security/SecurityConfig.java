package com.ykw.article.security;

import com.ykw.common.filter.LoggingFilter;
import com.ykw.common.filter.RequestContextFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            RequestContextFilter requestContextFilter,
                                            LoggingFilter loggingFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - read
                        .requestMatchers(HttpMethod.GET, "/api/articles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()

                        // Protected endpoints - write
                        .requestMatchers(HttpMethod.POST, "/api/articles").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/articles/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/*").authenticated()

                        // Tag operations - protected
                        .requestMatchers("/api/articles/*/tags/**").authenticated()

                        // Feed (personalized -> must be authenticated)
                        .requestMatchers(HttpMethod.GET, "/api/articles/feed").authenticated()

                        // Everything else
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .addFilterBefore(requestContextFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(loggingFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}