package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.service.application.ExternalUserCommandService;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;
    private final ExternalUserCommandService externalUserCommandService;
    private final ObjectMapper objectMapper;

    private static final String EVENT_NAME = "notifications";

    /**
     * 실시간 알림 발송 (RabbitMQ 유지)
     */
    @RabbitListener(queues = RabbitConfig.USER_ROLE_UPDATED_QUEUE)
    public void handleRoleUpdatedEvent(RoleUpdatedEvent event) {
        log.info("Received RoleUpdatedEvent from RabbitMQ: {}", event);
        if (event.userId() == null) {
            log.warn("RoleUpdatedEvent userId is null: {}", event);
            return;
        }
        NotificationDto notificationDto = notificationCommandService.create(
                event.userId(),
                "권한이 %s(으)로 변경되었어요.".formatted(event.role()),
                null,
                NotificationLevel.INFO
        );
        if (notificationDto != null) {
            sseService.send(notificationDto, EVENT_NAME, event.userId());
        }
    }

    @RabbitListener(queues = RabbitConfig.USER_FOLLOWED_QUEUE)
    public void handleFollowedEvent(FollowedEvent event) {
        log.info("Received FollowedEvent from RabbitMQ: {}", event);
        if (event.userId() == null) {
            log.warn("FollowedEvent userId is null: {}", event);
            return;
        }

        NotificationDto notificationDto = notificationCommandService.create(
                event.userId(),
                "%s님이 팔로우했어요.".formatted(event.followerName()),
                null,
                NotificationLevel.INFO
        );
        if (notificationDto != null) {
            sseService.send(notificationDto, EVENT_NAME, event.userId());
        }
    }

    /**
     * CQRS 프로필 동기화 (RabbitMQ 리스너)
     */
    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    public void handleProfileCreated(UserProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-notification: {}", event);
        if (event.userId() == null) {
            log.warn("UserProfileCreatedEvent userId is null: {}", event);
            return;
        }
        externalUserCommandService.createOrUpdateProfile(event.userId(), event.name(), event.imageKey());
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    public void handleProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ in mopl-notification: {}", event);
        if (event.userId() == null) {
            log.warn("UserProfileUpdatedEvent userId is null: {}", event);
            return;
        }
        externalUserCommandService.createOrUpdateProfile(event.userId(), event.name(), event.imageKey());
    }

    /**
     * CQRS 프로필 동기화 (Kafka 리스너)
     */
    @KafkaListener(topics = "user.profile.events", groupId = "notification-profile-group")
    public void handleProfileEventsFromKafka(String payload) {
        log.info("Received UserProfileKafkaEvent from Kafka in mopl-notification: {}", payload);
        try {
            UserProfileKafkaEvent event = objectMapper.readValue(payload, UserProfileKafkaEvent.class);
            if (event.userId() != null) {
                externalUserCommandService.createOrUpdateProfile(event.userId(), event.name(), event.imageKey());
            }
        } catch (Exception e) {
            log.error("Failed to process UserProfileKafkaEvent in mopl-notification: {}", e.getMessage(), e);
        }
    }
}
