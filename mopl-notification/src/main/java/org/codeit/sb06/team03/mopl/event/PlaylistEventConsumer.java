package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
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
    public void consumeSubscriptionCreatedEvent(SubscriptionCreatedEvent event) {
        log.info("Received playlist subscription message: playlistId={}, subscriberName={}",
                event.playlistId(), event.subscriberName());

        if (event.ownerId() == null) {
            log.warn("SubscriptionCreatedEvent ownerId is null: {}", event);
            return;
        }

        NotificationDto notificationDto = notificationCommandService.create(
                event.ownerId(),
                "%s 님이 내 플레이리스트 %s 을(를) 구독했어요.".formatted(event.subscriberName(), event.playlistTitle()),
                null,
                NotificationLevel.INFO
        );

        if (notificationDto != null) {
            sseService.send(notificationDto, EVENT_NAME, event.ownerId());
        }
    }

    @RabbitListener(queues = RabbitConfig.PLAYLIST_CREATED_QUEUE)
    public void consumePlaylistCreatedEvent(PlaylistCreatedEvent event) {
        log.info("Received PlaylistCreatedEvent from RabbitMQ: playlistId={}", event.playlistId());

        if (event.followerIds() == null || event.followerIds().isEmpty()) {
            return;
        }

        final String notificationTitle = "%s 님이 새 플레이리스트 '%s'를 생성했어요."
                .formatted(event.ownerName(), event.playlistTitle());

        List<NotificationDto> notifications = notificationCommandService.createAll(
                event.followerIds(),
                notificationTitle,
                null,
                NotificationLevel.INFO
        );
        Map<UUID, Object> data = notifications.stream()
                .filter(dto -> dto.receiverId() != null)
                .collect(Collectors.toMap(NotificationDto::receiverId, dto -> dto));
        if (!data.isEmpty()) {
            sseService.sendAll(data, EVENT_NAME);
        }
    }

    @RabbitListener(queues = RabbitConfig.CURATION_ADDED_QUEUE)
    public void consumeCurationAddedEvent(CurationAddedEvent event) {
        log.info("Received CurationAddedEvent from RabbitMQ: playlistId={}", event.playlistId());

        if (event.subscriberIds() == null || event.subscriberIds().isEmpty()) {
            return;
        }

        final String notificationTitle = "%s 플레이리스트에 컨텐츠가 추가되었어요."
                .formatted(event.playlistTitle());

        List<NotificationDto> notifications = notificationCommandService.createAll(
                event.subscriberIds(),
                notificationTitle,
                event.contentTitle(),
                NotificationLevel.INFO
        );
        Map<UUID, Object> data = notifications.stream()
                .filter(dto -> dto.receiverId() != null)
                .collect(Collectors.toMap(NotificationDto::receiverId, dto -> dto));
        if (!data.isEmpty()) {
            sseService.sendAll(data, EVENT_NAME);
        }
    }
}
