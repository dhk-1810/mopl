package org.codeit.sb06.team03.mopl.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.content.domain.event.ContentDeletedEvent;
import org.codeit.sb06.team03.mopl.content.domain.event.ContentCreatedEvent;
import org.codeit.sb06.team03.mopl.content.domain.event.ContentUpdatedEvent;
import org.codeit.sb06.team03.mopl.content.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String ROUTING_KEY_CONTENT_CREATED = "content.created";
    public static final String ROUTING_KEY_CONTENT_UPDATED = "content.updated";
    public static final String ROUTING_KEY_CONTENT_DELETED = "content.deleted";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentCreatedEvent(ContentCreatedEvent event) {
        log.info("Publishing ContentCreatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_CREATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentUpdatedEvent(ContentUpdatedEvent event) {
        log.info("Publishing ContentUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_UPDATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleContentDeletedEvent(ContentDeletedEvent event) {
        log.info("Publishing ContentDeletedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                ROUTING_KEY_CONTENT_DELETED,
                event
        );
    }
}
