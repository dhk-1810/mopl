package org.codeit.sb06.team03.mopl.notification.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.notification.application.in.CreateNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.infra.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.infra.messaging.PlaylistSubscribedMessage;
import org.codeit.sb06.team03.mopl.sse.application.SseUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaylistNotificationConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final SseUseCase sseUseCase;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.PLAYLIST_SUBSCRIBED_QUEUE)
    public void consumeSubscriptionCreatedEvent(PlaylistSubscribedMessage message) {
        log.info("Received playlist subscription message: playlistId={}, subscriberName={}", 
                message.getPlaylistId(), message.getSubscriberName());

        final String subscriberName = message.getSubscriberName();
        final String playlistTitle = message.getPlaylistTitle();
        
        NotificationDto notificationDto = createNotificationUseCase.create(
                message.getOwnerId(),
                "%s 님이 내 플레이리스트 %s 을(를) 구독했어요.".formatted(subscriberName, playlistTitle),
                null,
                NotificationLevel.INFO
        );
        
        sseUseCase.send(notificationDto, EVENT_NAME, message.getOwnerId());
    }
}
