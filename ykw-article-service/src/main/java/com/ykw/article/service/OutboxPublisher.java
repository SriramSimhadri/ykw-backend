package com.ykw.article.service;

import com.ykw.article.model.outbox.OutboxEvent;
import com.ykw.article.model.outbox.OutboxEventStatus;
import com.ykw.article.repository.OutboxRepository;
import com.ykw.common.logging.LogEvent;
import com.ykw.common.logging.LogUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.ykw.common.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxPublisher self;

    @Scheduled(fixedDelay = 10000)
    public void publish() {

        LogUtil.debug(LogEvent.create("PUBLISHING_ARTICLE_OUTBOX_EVENTS")
                .add(EVENT_TOPIC, ARTICLE_EVENTS_TOPIC));

        List<OutboxEvent> events = self.fetchAndMarkProcessing(50);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(ARTICLE_EVENTS_TOPIC, event.getAggregateId(), event.getPayload()).get();
                self.markSent(event);
            } catch (Exception e) {
                LogUtil.error(LogEvent.create("PUBLISHING_ARTICLE_OUTBOX_EVENT_FAILED")
                        .add(ID, event.getId())
                        .add(EVENT_ID, event.getEventId())
                        .add(EVENT_TYPE, event.getEventType())
                        .add(EVENT_TOPIC, ARTICLE_EVENTS_TOPIC));
                self.markFailed(event);
            }
        }
    }

    /**
     * Mark processing events
     * @param limit number of records
     * @return
     */
    @Transactional
    public List<OutboxEvent> fetchAndMarkProcessing(int limit) {
        List<OutboxEvent> events = repository.fetchBatchForUpdate(limit);

        events.forEach(e -> {
            e.setStatus(OutboxEventStatus.PROCESSING);
            e.setUpdatedAt(Instant.now());
        });

        return events;
    }

    /**
     * Mark sent events
     * @param event - Outbox events
     */
    @Transactional
    public void markSent(OutboxEvent event) {
        repository.updateStatus(event.getId(), OutboxEventStatus.SENT, Instant.now(), event.getRetries());

    }


    /**
     * Marks failed events
     * @param event - Outbox events
     */
    @Transactional
    public void markFailed(OutboxEvent event) {
        repository.updateStatus(event.getId(), OutboxEventStatus.FAILED, Instant.now(), event.getRetries() + 1);
    }
}