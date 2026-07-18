package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.NotificationDto;
import org.codeit.sb06.team03.mopl.service.SseService;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class PlaylistEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.PLAYLIST_SUBSCRIBED_QUEUE)
    public void consumeSubscriptionCreatedEvent(PlaylistEvent.SubscriptionCreatedEvent event) {
        log.info("Received playlist subscription message: playlistId={}, subscriberName={}",
                event.getPlaylistId(), event.getSubscriberName());

        NotificationDto notificationDto = notificationCommandService.create(
                event.getOwnerId(),
                "%s 님이 내 플레이리스트 %s 을(를) 구독했어요.".formatted(event.getSubscriberName(), event.getPlaylistTitle()),
                null,
                NotificationLevel.INFO
        );

        sseService.send(notificationDto, EVENT_NAME, event.getOwnerId());
    }

    @RabbitListener(queues = RabbitConfig.PLAYLIST_CREATED_QUEUE)
    public void consumePlaylistCreatedEvent(PlaylistEvent.PlaylistCreatedEvent event) {
        log.info("Received PlaylistCreatedEvent from RabbitMQ: playlistId={}", event.getPlaylistId());

        final String notificationTitle = "%s 님이 새 플레이리스트 '%s'를 생성했어요."
                .formatted(event.getOwnerName(), event.getPlaylistTitle());

        List<NotificationDto> notifications = notificationCommandService.createAll(
                event.getFollowerIds(),
                notificationTitle,
                null,
                NotificationLevel.INFO
        );
        Map<UUID, Object> data = notifications.stream()
                .collect(Collectors.toMap(NotificationDto::receiverId, dto -> dto));
        sseService.sendAll(data, EVENT_NAME);
    }

    @RabbitListener(queues = RabbitConfig.CURATION_ADDED_QUEUE)
    public void consumeCurationAddedEvent(PlaylistEvent.CurationAddedEvent event) {
        log.info("Received CurationAddedEvent from RabbitMQ: playlistId={}", event.getPlaylistId());

        final String notificationTitle = "%s 플레이리스트에 컨텐츠가 추가되었어요."
                .formatted(event.getPlaylistTitle());

        List<NotificationDto> notifications = notificationCommandService.createAll(
                event.getSubscriberIds(),
                notificationTitle,
                event.getContentTitle(),
                NotificationLevel.INFO
        );
        Map<UUID, Object> data = notifications.stream()
                .collect(Collectors.toMap(NotificationDto::receiverId, dto -> dto));
        sseService.sendAll(data, EVENT_NAME);
    }
}
