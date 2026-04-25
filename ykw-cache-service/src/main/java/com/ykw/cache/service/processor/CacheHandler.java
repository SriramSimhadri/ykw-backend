package com.ykw.cache.service.processor;

import com.ykw.common.event.Event;

public interface CacheHandler {

    void handle(Event<?> event);

}