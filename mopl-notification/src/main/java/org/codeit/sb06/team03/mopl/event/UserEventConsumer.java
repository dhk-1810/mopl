package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.application.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.notification.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.service.application.ExternalUserCommandService;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;
    private final ExternalUserCommandService externalUserCommandService;
    private final ExternalUserQueryService externalUserQueryService;

    private static final String EVENT_NAME = "notifications";

    /**
     * 알림 발송
     */
    @RabbitListener(queues = RabbitConfig.USER_ROLE_UPDATED_QUEUE)
    public void handleRoleUpdatedEvent(UserEvent.RoleUpdatedEvent event) {
        log.info("Received RoleUpdatedEvent from RabbitMQ: {}", event);
        NotificationDto notificationDto = notificationCommandService.create(
                event.getAccountId(),
                "권한이 %s(으)로 변경되었어요.".formatted(event.getRole()),
                null,
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME, event.getAccountId());
    }

    @RabbitListener(queues = RabbitConfig.USER_FOLLOWED_QUEUE)
    public void handleFollowedEvent(UserEvent.FollowedEvent event) {
        log.info("Received FollowedEvent from RabbitMQ: {}", event);

        ExternalUserView profile = externalUserQueryService.getProfile(event.getFollowerId());
        String name = (profile != null) ? profile.getName() : "누군가";

        NotificationDto notificationDto = notificationCommandService.create(
                event.getFolloweeId(),
                "%s님이 팔로우했어요.".formatted(name),
                null,
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME, event.getFolloweeId());
    }

    /**
     * CQRS
     */
    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    public void handleProfileCreated(UserEvent.ProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-notification: {}", event);
        externalUserCommandService.createOrUpdateProfile(event.getUserId(), event.getName(), event.getImageKey());
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    public void handleProfileUpdated(UserEvent.ProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ in mopl-notification: {}", event);
        externalUserCommandService.createOrUpdateProfile(event.getUserId(), event.getName(), event.getImageKey());
    }
}
