package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.service.application.OutboxService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventPublisher {

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentUpdatedAfterCommit(ContentUpdatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(
                    KafkaConfig.TOPIC_CONTENT_METADATA_EVENTS,
                    event.contentId().toString(),
                    payload
            );
            log.info("Published ContentUpdatedEvent to Kafka for contentId: {}", event.contentId());
        } catch (Exception e) {
            log.error("Failed to publish ContentUpdatedEvent to Kafka: {}", e.getMessage(), e);
        }
    }

    public void publishContentDeletionSagaStart(ContentDeletionSagaEvent event) {
        log.info("Saving ContentDeletionSagaEvent START to Outbox table: {}", event);
        outboxService.saveEvent(
                "CONTENT_SAGA",
                event.sagaId().toString(),
                ContentDeletionSagaEvent.class.getName(),
                KafkaConfig.TOPIC_SAGA_START,
                event
        );
    }

    public void publishContentDeletionSagaCompensate(ContentDeletionSagaEvent event) {
        log.info("Saving ContentDeletionSagaEvent COMPENSATE to Outbox table: {}", event);
        outboxService.saveEvent(
                "CONTENT_SAGA",
                event.sagaId().toString(),
                ContentDeletionSagaEvent.class.getName(),
                KafkaConfig.TOPIC_SAGA_START,
                event
        );
    }
}
