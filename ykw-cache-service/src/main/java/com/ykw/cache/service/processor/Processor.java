package com.ykw.cache.service.processor;

import com.ykw.common.event.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class Processor {

    private final Map<String, CacheHandler> handlers;

    public void process(Event<?> event) {
        CacheHandler handler = handlers.get(event.getEntity().toLowerCase());
        if (handler == null) {
            throw new RuntimeException("No handler for eventType: " + event.getEventType());
        }
        handler.handle(event);
    }
}