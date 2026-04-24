package com.ykw.cache.service.processor;

import com.ykw.cache.service.model.ReceivedEvent;
import com.ykw.cache.service.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("article")
@RequiredArgsConstructor
public class ArticleCacheHandler implements CacheHandler {

    private final RedisCacheService redisService;

    @Override
    public void handle(ReceivedEvent event) {

        String key = "article:" + event.getAggregateId();

        if (event.getEventType().contains("DELETE")) {
            redisService.delete(key);
        } else {
            redisService.put(key, event.getPayload());
        }
    }
}
