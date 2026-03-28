package com.ykw.auth.configuration;

import com.ykw.common.filter.LoggingFilter;
import com.ykw.common.filter.RequestContextFilter;
import com.ykw.common.security.CurrentUserContext;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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

/*
    @Bean(name = "customRequestContextFilter")
    public FilterRegistrationBean<RequestContextFilter> customRequestContextFilter() {
        FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestContextFilter());
  //      registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
*/

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
