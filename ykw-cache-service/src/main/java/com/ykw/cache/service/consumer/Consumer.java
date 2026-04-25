package com.ykw.cache.service.consumer;

import com.ykw.common.event.Event;
import com.ykw.cache.service.processor.Processor;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
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

    @KafkaListener(topics = ARTICLE_EVENTS_TOPIC)
    public void consume(Event<?> event, Acknowledgment ack) {
        LogUtil.info(LogEvent.create("EVENT_RECEIVED").add("event_id", event.getEventId()));

        try {
            processor.process(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed processing event {}", event, e);

            // TODO: skip bad message (replace later with DLQ)
            ack.acknowledge();
        }
    }
}