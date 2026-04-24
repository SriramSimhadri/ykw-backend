package com.ykw.article.service;

import com.ykw.article.mapper.EventMapper;
import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ykw.common.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final EventMapper eventMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 10000)
    public void publish() {

        LogUtil.debug(LogEvent.create("PUBLISHING_ARTICLE_EVENTS")
                .add(EVENT_TOPIC, ARTICLE_EVENTS_TOPIC));

        List<OutboxEvent> events = outboxService.fetchAndMarkProcessing(50);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(ARTICLE_EVENTS_TOPIC, event.getAggregateId(), eventMapper.toEvent(event)).get();
                outboxService.markSent(event);
            } catch (Exception e) {
                LogUtil.error(LogEvent.create("PUBLISHING_ARTICLE_EVENT_FAILED")
                        .add(ID, event.getId())
                        .add(EVENT_ID, event.getEventId())
                        .add(EVENT_TYPE, event.getEventType())
                        .add(EVENT_TOPIC, ARTICLE_EVENTS_TOPIC));
                outboxService.markFailed(event);
            }
        }
    }
}