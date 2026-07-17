package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.follow.domain.event.FollowEvent;
import org.codeit.sb06.team03.mopl.notification.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.controller.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.service.NotificationCommandService;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.codeit.sb06.team03.mopl.notification.service.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.notification.domain.entity.cqrs.ExternalUserView;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FollowEventListener {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;
    private final ExternalUserQueryService externalUserQueryService;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.USER_FOLLOWED_QUEUE)
    public void handleFollowedEvent(FollowEvent.FollowedEvent event) {
        log.info("Received FollowedEvent from RabbitMQ: {}", event);
        
        // Fetch follower's name from local ExternalUserView cache
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
}
