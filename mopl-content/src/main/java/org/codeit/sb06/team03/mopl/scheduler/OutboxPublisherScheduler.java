package org.codeit.sb06.team03.mopl.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.OutboxEvent;
import org.codeit.sb06.team03.mopl.service.application.OutboxService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherScheduler {

    private final OutboxService outboxService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 3000) // 3초 주기 폴링
    public void processPendingOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxService.findPendingEvents(MAX_RETRIES, BATCH_SIZE);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("[Outbox Poller] Processing {} pending outbox event(s)", pendingEvents.size());
        for (OutboxEvent event : pendingEvents) {
            publishEvent(event);
        }
    }

    public void publishEvent(OutboxEvent event) {
        try {
            Class<?> eventClass = Class.forName(event.getEventType());
            Object eventObject = objectMapper.readValue(event.getPayload(), eventClass);
            CorrelationData correlationData = new CorrelationData("outbox-" + event.getId());

            rabbitTemplate.convertAndSend(
                    event.getExchange(),
                    event.getRoutingKey(),
                    eventObject,
                    correlationData
            );

            outboxService.markAsPublished(event.getId());
            log.info("[Outbox Poller] Successfully published event [id: {}, type: {}, routingKey: {}]",
                    event.getId(), event.getEventType(), event.getRoutingKey());
        } catch (Exception e) {
            log.error("[Outbox Poller] Failed to publish outbox event id: {}, error: {}", event.getId(), e.getMessage());
            outboxService.recordFailure(event.getId(), e.getMessage(), MAX_RETRIES);
        }
    }
}
