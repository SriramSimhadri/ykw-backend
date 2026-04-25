package com.ykw.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ArticleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleServiceApplication.class, args);
    }
}