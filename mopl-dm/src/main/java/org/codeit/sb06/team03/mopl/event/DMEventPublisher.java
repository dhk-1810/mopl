package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DMEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String ROUTING_KEY_MESSAGE_SENT = "dm.message-sent";
    public static final String ROUTING_KEY_NOTIFICATION_REQUIRED = "dm.notification-required";
    public static final String ROUTING_KEY_SSE_SEND = "dm.sse-send";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSentEvent(DMEvent.MessageSentEvent event) {
        log.info("Publishing MessageSentEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.DM_EXCHANGE,
                ROUTING_KEY_MESSAGE_SENT,
                event
        );
    }

}

