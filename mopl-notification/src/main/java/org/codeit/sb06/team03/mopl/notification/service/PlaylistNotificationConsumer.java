package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.notification.service.NotificationCommandService;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.controller.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.service.PlaylistSubscribedMessage;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaylistNotificationConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.PLAYLIST_SUBSCRIBED_QUEUE)
    public void consumeSubscriptionCreatedEvent(PlaylistSubscribedMessage message) {
        log.info("Received playlist subscription message: playlistId={}, subscriberName={}", 
                message.getPlaylistId(), message.getSubscriberName());

        final String subscriberName = message.getSubscriberName();
        final String playlistTitle = message.getPlaylistTitle();
        
        NotificationDto notificationDto = notificationCommandService.create(
                message.getOwnerId(),
                "%s 님이 내 플레이리스트 %s 을(를) 구독했어요.".formatted(subscriberName, playlistTitle),
                null,
                NotificationLevel.INFO
        );
        
        sseService.send(notificationDto, EVENT_NAME, message.getOwnerId());
    }
}
