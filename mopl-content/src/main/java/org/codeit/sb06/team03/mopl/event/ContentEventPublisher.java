package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String ROUTING_KEY_CONTENT_UPDATED = "content.updated";
    public static final String ROUTING_KEY_CONTENT_DELETED = "content.deleted";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentUpdatedEvent(ContentUpdatedEvent event) {
        log.info("Publishing ContentUpdatedEvent to RabbitMQ: {}", event);
        CorrelationData correlationData = new CorrelationData("content-updated-" + event.contentId() + "-" + UUID.randomUUID());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_UPDATED,
                event,
                correlationData
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentDeletedEvent(ContentDeletedEvent event) {
        log.info("Publishing ContentDeletedEvent to RabbitMQ: {}", event);
        CorrelationData correlationData = new CorrelationData("content-deleted-" + event.contentId() + "-" + UUID.randomUUID());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_DELETED,
                event,
                correlationData
        );
    }

    public void publishContentDeletionSagaStart(ContentDeletionSagaEvent event) {
        log.info("Publishing ContentDeletionSagaEvent START to RabbitMQ: {}", event);
        CorrelationData correlationData = new CorrelationData("saga-start-" + event.sagaId());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                RabbitConfig.ROUTING_KEY_SAGA_START,
                event,
                correlationData
        );
    }
}
