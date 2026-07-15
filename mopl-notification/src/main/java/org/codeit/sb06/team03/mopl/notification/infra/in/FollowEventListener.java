package org.codeit.sb06.team03.mopl.notification.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.follow.domain.event.FollowEvent;
import org.codeit.sb06.team03.mopl.notification.infra.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.application.in.CreateNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.sse.application.SseUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FollowEventListener {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final SseUseCase sseUseCase;
    private final GetProfileUseCase getProfileUseCase;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.USER_FOLLOWED_QUEUE)
    public void handleFollowedEvent(FollowEvent.FollowedEvent event) {
        log.info("Received FollowedEvent from RabbitMQ: {}", event);
        Profile profile = getProfileUseCase.getById(event.getFolloweeId());

        NotificationDto notificationDto = createNotificationUseCase.create(
                event.getFolloweeId(),
                "%s님이 팔로우했어요.".formatted(profile.getName()),
                null,
                NotificationLevel.INFO
        );
        sseUseCase.send(notificationDto, EVENT_NAME, event.getFolloweeId());
    }
}

