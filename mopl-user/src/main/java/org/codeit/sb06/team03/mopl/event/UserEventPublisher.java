package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.security.jwt.registry.JwtRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final JwtRegistry jwtRegistry;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String KAFKA_TOPIC_PROFILE_EVENTS = "user.profile.events";

    /**
     * 알림 발송
     */

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoleUpdatedEvent(RoleUpdatedEvent event) {
        log.info("Publishing RoleUpdatedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                RabbitConfig.USER_ROLE_UPDATED_ROUTING_KEY,
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowedEvent(FollowedEvent event) {
        log.info("Publishing FollowedEvent to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitConfig.USER_EXCHANGE,
                RabbitConfig.USER_FOLLOWED_ROUTING_KEY,
                event
        );
        try {
            FollowKafkaEvent kafkaEvent = FollowKafkaEvent.followed(event.followeeId(), event.followerId());
            kafkaTemplate.send(org.codeit.sb06.team03.mopl.config.KafkaConfig.TOPIC_USER_FOLLOW_EVENTS, event.followeeId().toString(), kafkaEvent);
            log.info("Publishing FollowKafkaEvent (FOLLOWED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish FollowedEvent to Kafka: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUnfollowedEvent(UnfollowedEvent event) {
        try {
            FollowKafkaEvent kafkaEvent = FollowKafkaEvent.unfollowed(event.followeeId(), event.followerId());
            kafkaTemplate.send(org.codeit.sb06.team03.mopl.config.KafkaConfig.TOPIC_USER_FOLLOW_EVENTS, event.followeeId().toString(), kafkaEvent);
            log.info("Publishing FollowKafkaEvent (UNFOLLOWED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish UnfollowedEvent to Kafka: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAccountLockUpdatedEvent(AccountLockUpdatedEvent event) {
        log.info("Handling AccountLockUpdatedEvent: {}", event);
        if (event.locked()) {
            jwtRegistry.invalidateAllByUserId(event.accountId());
            rabbitTemplate.convertAndSend(
                    RabbitConfig.WATCHING_SESSION_EXCHANGE,
                    RabbitConfig.WATCHING_SESSION_DELETE_ROUTING_KEY,
                    new WatchingSessionDeleteRequestEvent(event.accountId())
            );
        }
    }

    /**
     * CQRS 복제테이블 동기화 (Kafka)
     */

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileCreatedEvent(ProfileCreatedEvent event) {
        try {
            ProfileKafkaEvent kafkaEvent = ProfileKafkaEvent.created(event.userId(), event.name(), event.imageKey());
            kafkaTemplate.send(KAFKA_TOPIC_PROFILE_EVENTS, event.userId().toString(), kafkaEvent);
            log.info("Publishing ProfileKafkaEvent (CREATED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish ProfileCreatedEvent to Kafka: {}", e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProfileUpdatedEvent(ProfileUpdatedEvent event) {
        try {
            ProfileKafkaEvent kafkaEvent = ProfileKafkaEvent.updated(event.userId(), event.name(), event.imageKey());
            kafkaTemplate.send(KAFKA_TOPIC_PROFILE_EVENTS, event.userId().toString(), kafkaEvent);
            log.info("Publishing ProfileKafkaEvent (UPDATED) to Kafka: {}", kafkaEvent);
        } catch (Exception e) {
            log.warn("Failed to publish ProfileUpdatedEvent to Kafka: {}", e.getMessage());
        }
    }

}
