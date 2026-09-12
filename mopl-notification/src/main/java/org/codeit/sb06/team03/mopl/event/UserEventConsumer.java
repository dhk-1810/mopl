package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;
    private final ProfileQueryService profileQueryService;

    private static final String EVENT_NAME = "notifications";

    @EventListener
    public void handleRoleUpdatedEvent(UserEvent.RoleUpdatedEvent event) {
        log.info("Handling RoleUpdatedEvent in notification: {}", event);
        NotificationDto notificationDto = notificationCommandService.create(
                event.userId(),
                "권한이 %s(으)로 변경되었어요.".formatted(event.role()),
                null,
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME, event.userId());
    }

    @EventListener
    public void handleFollowedEvent(UserEvent.FollowedEvent event) {
        log.info("Handling FollowedEvent in notification: {}", event);

        String name = "누군가";
        try {
            Profile profile = profileQueryService.getById(event.followerId());
            if (profile != null) {
                name = profile.getName();
            }
        } catch (Exception ignored) {
        }

        NotificationDto notificationDto = notificationCommandService.create(
                event.userId(),
                "%s님이 팔로우했어요.".formatted(name),
                null,
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME, event.userId());
    }
}
