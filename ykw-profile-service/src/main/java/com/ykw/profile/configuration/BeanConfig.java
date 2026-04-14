package com.ykw.profile.configuration;

import com.ykw.common.filter.LoggingFilter;
import com.ykw.common.filter.RequestContextFilter;
import com.ykw.common.security.CurrentUserContext;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public CurrentUserContext currentUserContext() {
        return new CurrentUserContext();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("ykw-profile-service");
    }

    @Bean(name = "requestLoggingFilter")
    public LoggingFilter requestLoggingFilter() {
        return new LoggingFilter();
    }

    @Bean(name = "customRequestContextFilter")
    public RequestContextFilter customRequestContextFilter() {
        return new RequestContextFilter();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer serializer = new StringRedisSerializer();

        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();

        return template;
    }
}
