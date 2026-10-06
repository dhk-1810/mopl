package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.kafka.annotation.KafkaListener;
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

    private final WatchingSessionCommandService watchingSessionCommandService;
    private final WatchingSessionRepository watchingSessionRepository;
    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ProfileQueryService profileQueryService;
    private final ExternalImageQueryService externalImageQueryService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.WS_CREATE_QUEUE)
    public void handleWatchingSessionCreate(
            WatchingSessionCreateRequestEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received WatchingSessionCreateRequestEvent from RabbitMQ: {}", event);
        try {
            watchingSessionCommandService.createWithId(
                    event.sessionId(),
                    event.contentId(),
                    event.watcherId(),
                    event.createdAt()
            );
            log.info("Successfully created watching session from queue for watcher: {}", event.watcherId());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to create watching session asynchronously from event: {}", event, e);
            channel.basicReject(deliveryTag, false);
        }
    }

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

    @KafkaListener(topics = RabbitConfig.ROUTING_KEY_SAGA_START, groupId = "${spring.kafka.consumer.group-id:live-chat-group}")
    public void handleContentDeletionSaga(String payload) {
        log.info("Received ContentDeletionSagaEvent START from Kafka in mopl-live-chat: {}", payload);

        ContentDeletionSagaEvent event;
        try {
            event = objectMapper.readValue(payload, ContentDeletionSagaEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize ContentDeletionSagaEvent from Kafka payload: {}", payload, e);
            return;
        }

        String inboxKey = "inbox:saga-start:" + event.sagaId() + ":watching-session";
        Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(inboxKey, "PROCESSED", java.time.Duration.ofDays(7));
        if (Boolean.FALSE.equals(isFirst)) {
            log.info("[Inbox] Saga start already processed in watching-session. Skipping duplicate message: {}", inboxKey);
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "WATCHING_SESSION"));
            return;
        }

        try {
            // 해당 contentId와 연관된 시청 세션 정리 (LiveChatRoom 단위 삭제 또는 watcher 삭제 연동)
            // mopl-watching-session 은 Redis 데이터를 기반으로 세션을 삭제
            watchingSessionCommandService.deleteByLiveChatRoomId(event.contentId());

            // Saga 성공 이벤트 응답 (mopl-content로 전파)
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "WATCHING_SESSION"));
        } catch (Exception e) {
            log.error("Failed to delete watching session for contentId: {}", event.contentId(), e);
            // 실패 시 inbox 키 제거하여 재시도 허용
            redisTemplate.delete(inboxKey);
            // 실패 응답 전송
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "WATCHING_SESSION", e.getMessage()));
            throw new RuntimeException("Error processing ContentDeletionSagaEvent in watching session", e);
        }
    }

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        org.springframework.amqp.rabbit.connection.CorrelationData correlationData =
                new org.springframework.amqp.rabbit.connection.CorrelationData("ws-saga-response-" + event.sagaId() + "-" + event.status());
        rabbitTemplate.convertAndSend(
                RabbitConfig.CONTENT_EXCHANGE,
                RabbitConfig.ROUTING_KEY_SAGA_RESPONSE,
                event,
                correlationData
        );
    }
}
