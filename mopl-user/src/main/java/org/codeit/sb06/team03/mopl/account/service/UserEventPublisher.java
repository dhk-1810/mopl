package org.codeit.sb06.team03.mopl.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.account.domain.event.AccountEvent;
import org.codeit.sb06.team03.mopl.account.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.follow.domain.event.FollowEvent;
import org.codeit.sb06.team03.mopl.profile.domain.event.UserEvent.UserProfileCreatedEvent;
import org.codeit.sb06.team03.mopl.profile.domain.event.UserEvent.UserProfileUpdatedEvent;
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
    public static final String ROUTING_KEY_PROFILE_CREATED = "user.profile-created";
    public static final String ROUTING_KEY_PROFILE_UPDATED = "user.profile-updated";

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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileCreatedEvent(UserProfileCreatedEvent event) {
        log.info("Publishing UserProfileCreatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_PROFILE_CREATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileUpdatedEvent(UserProfileUpdatedEvent event) {
        log.info("Publishing UserProfileUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_PROFILE_UPDATED,
                event
        );
    }
}
