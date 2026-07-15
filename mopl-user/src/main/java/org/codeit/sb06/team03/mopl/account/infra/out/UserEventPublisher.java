package org.codeit.sb06.team03.mopl.account.infra.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.account.domain.event.AccountEvent;
import org.codeit.sb06.team03.mopl.account.infra.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.follow.domain.event.FollowEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String ROUTING_KEY_ROLE_UPDATED = "user.role-updated";
    public static final String ROUTING_KEY_FOLLOWED = "user.followed";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleUpdatedEvent(AccountEvent.RoleUpdatedEvent event) {
        log.info("Publishing RoleUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_ROLE_UPDATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowedEvent(FollowEvent.FollowedEvent event) {
        log.info("Publishing FollowedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_FOLLOWED,
                event
        );
    }
}
