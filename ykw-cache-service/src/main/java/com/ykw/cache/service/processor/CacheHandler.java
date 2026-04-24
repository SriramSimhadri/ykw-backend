package com.ykw.cache.service.processor;

import com.ykw.cache.service.model.ReceivedEvent;

public interface CacheHandler {

    void handle(ReceivedEvent event);

}