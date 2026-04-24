package com.ykw.cache.service.processor;

import com.ykw.cache.service.model.ReceivedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class Processor {

    private final Map<String, CacheHandler> handlers;

    public void process(ReceivedEvent event) {

        CacheHandler handler = handlers.get(event.getEntity());

        if (handler == null) {
            throw new RuntimeException("No handler for entity: " + event.getEntity());
        }

        handler.handle(event);
    }
}