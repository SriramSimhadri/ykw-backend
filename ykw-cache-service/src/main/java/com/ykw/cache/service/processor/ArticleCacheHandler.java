package com.ykw.cache.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.cache.service.service.RedisCacheService;
import com.ykw.common.event.Event;
import com.ykw.common.event.ArticleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("article")
@RequiredArgsConstructor
public class ArticleCacheHandler implements CacheHandler {

    private final RedisCacheService redisService;

    private final ObjectMapper objectMapper;

    @Override
    public void handle(Event<?> event) {

        ArticleEvent article = objectMapper.convertValue(event.getPayload(), ArticleEvent.class);

        String keyId = "article:" + article.getId();
        String keySlug = "article:slug:" + article.getSlug();

        if ("ARTICLE_DELETED".equals(event.getEventType())) {
            redisService.delete(keyId);
            redisService.delete(keySlug);
        } else {
            redisService.put(keyId, article);
            redisService.put(keySlug, keyId);
        }
    }
}