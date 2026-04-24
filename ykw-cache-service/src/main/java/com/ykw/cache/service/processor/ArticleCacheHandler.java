package com.ykw.cache.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.cache.service.model.ReceivedEvent;
import com.ykw.cache.service.service.RedisCacheService;
import com.ykw.common.payload.ArticleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("article")
@RequiredArgsConstructor
public class ArticleCacheHandler implements CacheHandler {

    private final RedisCacheService redisService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(ReceivedEvent event) {

        try {
            ArticleEvent article = objectMapper.readValue(event.getPayload(), ArticleEvent.class);
            String id = article.getId();
            String slug = article.getSlug();

            String keyId = "article:" + id;

            String keySlug = "article:slug:" + slug;

            if ("ARTICLE_DELETED".equals(event.getEventType())) {

                redisService.delete(keyId);
                redisService.delete(keySlug);
            } else {
                //store article
                redisService.put(keyId, article);
                //store index
                redisService.put(keySlug, keyId);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize article", e);
        }
    }
}
