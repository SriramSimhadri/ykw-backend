package com.ykw.profile.config;

import com.ykw.common.filter.LoggingFilter;
import com.ykw.common.filter.RequestContextFilter;
import com.ykw.common.security.CurrentUserContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public CurrentUserContext currentUserContext() {
        return new CurrentUserContext();
    }

    @Bean(name = "requestLoggingFilter")
    public LoggingFilter requestLoggingFilter() {
        return new LoggingFilter();
    }

    @Bean(name = "customRequestContextFilter")
    public RequestContextFilter customRequestContextFilter() {
        return new RequestContextFilter();
    }
}
