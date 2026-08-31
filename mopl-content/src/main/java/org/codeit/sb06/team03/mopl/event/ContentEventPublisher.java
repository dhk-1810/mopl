package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.service.application.OutboxService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;

    public static final String ROUTING_KEY_CONTENT_UPDATED = "content.updated";
    public static final String ROUTING_KEY_CONTENT_DELETED = "content.deleted";

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleContentUpdatedBeforeCommit(ContentUpdatedEvent event) {
        log.info("Saving ContentUpdatedEvent to Outbox table: {}", event);
        outboxService.saveEvent(
                "CONTENT",
                event.contentId().toString(),
                ContentUpdatedEvent.class.getName(),
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_UPDATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleContentDeletedBeforeCommit(ContentDeletedEvent event) {
        log.info("Saving ContentDeletedEvent to Outbox table: {}", event);
        outboxService.saveEvent(
                "CONTENT",
                event.contentId().toString(),
                ContentDeletedEvent.class.getName(),
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_DELETED,
                event
        );
    }

    public void publishContentDeletionSagaStart(ContentDeletionSagaEvent event) {
        log.info("Saving ContentDeletionSagaEvent START to Outbox table: {}", event);
        outboxService.saveEvent(
                "CONTENT_SAGA",
                event.sagaId().toString(),
                ContentDeletionSagaEvent.class.getName(),
                RabbitConfig.CONTENT_EXCHANGE,
                RabbitConfig.ROUTING_KEY_SAGA_START,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentUpdatedAfterCommit(ContentUpdatedEvent event) {
        try {
            log.info("Attempting immediate publish for ContentUpdatedEvent to RabbitMQ: {}", event);
            CorrelationData correlationData = new CorrelationData("content-updated-" + event.contentId() + "-" + UUID.randomUUID());
            rabbitTemplate.convertAndSend(
                    RabbitConfig.CONTENT_EXCHANGE,
                    ROUTING_KEY_CONTENT_UPDATED,
                    event,
                    correlationData
            );
        } catch (Exception e) {
            log.warn("Immediate publish failed for ContentUpdatedEvent. Outbox poller will retry. Cause: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentDeletedAfterCommit(ContentDeletedEvent event) {
        try {
            log.info("Attempting immediate publish for ContentDeletedEvent to RabbitMQ: {}", event);
            CorrelationData correlationData = new CorrelationData("content-deleted-" + event.contentId() + "-" + UUID.randomUUID());
            rabbitTemplate.convertAndSend(
                    RabbitConfig.CONTENT_EXCHANGE,
                    ROUTING_KEY_CONTENT_DELETED,
                    event,
                    correlationData
            );
        } catch (Exception e) {
            log.warn("Immediate publish failed for ContentDeletedEvent. Outbox poller will retry. Cause: {}", e.getMessage());
        }
    }
}
