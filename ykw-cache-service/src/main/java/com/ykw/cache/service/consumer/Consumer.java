package com.ykw.cache.service.consumer;

import com.ykw.cache.service.model.ReceivedEvent;
import com.ykw.cache.service.processor.Processor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.ykw.common.constants.Constants.ARTICLE_EVENTS_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class Consumer {

    private final Processor processor;

    @KafkaListener(
            topics = { ARTICLE_EVENTS_TOPIC },
            groupId = "ykw-cache-service"
    )
    public void consume(ReceivedEvent event, Acknowledgment ack) {
        try {
            processor.process(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed processing event {}", event.getEventId(), e);
            throw e;
        }
    }
}