package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.security.jwt.registry.JwtRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final JwtRegistry jwtRegistry;
    private final org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    public static final String KAFKA_TOPIC_PROFILE_EVENTS = "user.profile.events";
    public static final String ROUTING_KEY_ROLE_UPDATED = "user.role-updated";
    public static final String ROUTING_KEY_FOLLOWED = "user.followed";
    public static final String ROUTING_KEY_PROFILE_CREATED = "user.profile-created";
    public static final String ROUTING_KEY_PROFILE_UPDATED = "user.profile-updated";

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleUpdatedEvent(RoleUpdatedEvent event) {
        log.info("Publishing RoleUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_ROLE_UPDATED,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowedEvent(FollowedEvent event) {
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
        try {
            UserProfileKafkaEvent kafkaEvent = UserProfileKafkaEvent.created(event.userId(), event.name(), event.imageKey());
            kafkaTemplate.send(KAFKA_TOPIC_PROFILE_EVENTS, event.userId().toString(), kafkaEvent);
            log.info("Publishing UserProfileKafkaEvent (CREATED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish UserProfileCreatedEvent to Kafka: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileUpdatedEvent(UserProfileUpdatedEvent event) {
        log.info("Publishing UserProfileUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                ROUTING_KEY_PROFILE_UPDATED,
                event
        );
        try {
            UserProfileKafkaEvent kafkaEvent = UserProfileKafkaEvent.updated(event.userId(), event.name(), event.imageKey());
            kafkaTemplate.send(KAFKA_TOPIC_PROFILE_EVENTS, event.userId().toString(), kafkaEvent);
            log.info("Publishing UserProfileKafkaEvent (UPDATED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish UserProfileUpdatedEvent to Kafka: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountLockUpdatedEvent(AccountLockUpdatedEvent event) {
        log.info("Handling AccountLockUpdatedEvent: {}", event);
        if (event.locked()) {
            jwtRegistry.invalidateAllByUserId(event.accountId());
            rabbitTemplate.convertAndSend(
                    RabbitConfig.WS_EXCHANGE,
                    RabbitConfig.WS_DELETE_ROUTING,
                    new WatchingSessionDeleteRequestEvent(event.accountId())
            );
        }
    }
}
