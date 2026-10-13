package org.codeit.sb06.team03.mopl.event;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.enums.WatchType;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionCommandService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WatchingSessionEventListener {

    private final WatchingSessionRepository watchingSessionRepository;
    private final WatchingSessionCommandService watchingSessionCommandService;
    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ProfileQueryService profileQueryService;
    private final ExternalImageQueryService externalImageQueryService;


    @RabbitListener(queues = RabbitConfig.WS_DELETE_QUEUE)
    public void handleWatchingSessionDelete(
            WatchingSessionDeleteRequestEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received WatchingSessionDeleteRequestEvent from RabbitMQ: {}", event);
        try {
            Optional<WatchingSessionReadModel> sessionOpt = Optional.empty();
            if (event.watcherId() != null) {
                sessionOpt = watchingSessionRepository.findReadModelByWatcherId(event.watcherId());
            }

            if (event.sessionId() != null) {
                watchingSessionCommandService.delete(event.sessionId());
            } else if (event.watcherId() != null) {
                watchingSessionCommandService.deleteByWatcherId(event.watcherId());
            }

            // 시청 중이던 세션이 존재하는 경우 방에 LEAVE presence 메시지 브로드캐스트
            if (sessionOpt.isPresent()) {
                WatchingSessionReadModel session = sessionOpt.get();
                UUID liveChatRoomId = session.liveChatRoomId();
                UUID watcherId = session.watcherId();

                ProfileReadModel profile = profileQueryService.getProfileReadModel(watcherId);
                String name = profile != null ? profile.name() : "Unknown User";
                String imageKey = profile != null ? profile.imageKey() : null;
                String profileImageUrl = externalImageQueryService.getPresignedUrl(imageKey);
                String destination = "/sub/contents/" + liveChatRoomId + "/watch";

                SendPresenceMessageCommand sendPresenceMessageCommand =
                        new SendPresenceMessageCommand(
                                UUID.randomUUID(),
                                Instant.now(),
                                watcherId,
                                name,
                                profileImageUrl,
                                WatchType.LEAVE.name(),
                                destination
                        );

                log.info("Broadcasting LEAVE Presence Message from event: liveChatRoomId={}, watcherId={}", liveChatRoomId, watcherId);
                liveChatRoomCommandService.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
            }

            log.info("Successfully deleted watching session from queue for event: {}", event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to delete watching session asynchronously from event: {}", event, e);
            channel.basicReject(deliveryTag, false);
        }
    }
}
