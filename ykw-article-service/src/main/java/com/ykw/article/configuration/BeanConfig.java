package com.ykw.article.configuration;

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

@Configuration
public class BeanConfig {

    @Bean
    public CurrentUserContext currentUserContext() {
        return new CurrentUserContext();
    }

    @Bean
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer("ykw-article-service");
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
