package com.ykw.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ykw.article.model.OutboxEvent;
import com.ykw.article.model.OutboxEventStatus;
import com.ykw.article.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.ykw.common.constants.Constants.ARTICLE_EVENTS_TOPIC;

@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxPublisher self;

    @Scheduled(fixedDelay = 5000)
    public void publish() {

        List<OutboxEvent> events = self.fetchAndMarkProcessing(50);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(ARTICLE_EVENTS_TOPIC, event.getAggregateId(), event.getPayload()).get();

                self.markSent(event.getId());

            } catch (Exception e) {

                self.markFailed(event.getId());
            }
        }
    }

    @Transactional
    public List<OutboxEvent> fetchAndMarkProcessing(int limit) {
        List<OutboxEvent> events = repository.fetchBatchForUpdate(limit);

        events.forEach(e -> {
            e.setStatus(OutboxEventStatus.PROCESSING);
            e.setUpdatedAt(Instant.now());
        });

        return events;
    }

    @Transactional
    public void markSent(Long id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus(OutboxEventStatus.SENT);
            e.setUpdatedAt(Instant.now());
        });
    }

    @Transactional
    public void markFailed(Long id) {
        repository.findById(id).ifPresent(e -> {
            e.setStatus(OutboxEventStatus.FAILED);
            e.setRetries(e.getRetries() + 1);
            e.setUpdatedAt(Instant.now());
        });
    }
}