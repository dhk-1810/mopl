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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

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

    @RabbitListener(queues = RabbitConfig.WS_CONTENT_SAGA_START_QUEUE)
    public void handleContentDeletionSagaRabbit(
            ContentDeletionSagaEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ContentDeletionSagaEvent START from RabbitMQ in mopl-live-chat: {}", event);
        try {
            processContentDeletionSaga(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error in rabbit saga listener in live-chat: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @KafkaListener(topics = RabbitConfig.ROUTING_KEY_SAGA_START, groupId = "${spring.kafka.consumer.group-id:live-chat-group}", autoStartup = "#{ '${mopl.saga.type:grpc}' == 'cdc-kafka' }")
    public void handleContentDeletionSagaKafka(
            @org.springframework.messaging.handler.annotation.Payload String payload,
            @Header(value = org.springframework.kafka.support.KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("Received ContentDeletionSagaEvent START from Kafka in mopl-live-chat: {}", payload);

        ContentDeletionSagaEvent event;
        try {
            event = objectMapper.readValue(payload, ContentDeletionSagaEvent.class);
        } catch (Exception e) {
            log.error("Failed to deserialize ContentDeletionSagaEvent from Kafka payload: {}", payload, e);
            handleDeserializationFailure(payload, key, e.getMessage());
            return;
        }

        processContentDeletionSaga(event);
    }

    private void handleDeserializationFailure(String payload, String key, String errorMsg) {
        try {
            UUID sagaId = null;
            if (key != null && !key.isBlank()) {
                try {
                    sagaId = UUID.fromString(key);
                } catch (IllegalArgumentException ignored) {}
            }
            if (sagaId == null && payload != null) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"sagaId\"\\s*:\\s*\"([0-9a-fA-F\\-]+)\"").matcher(payload);
                if (matcher.find()) {
                    sagaId = UUID.fromString(matcher.group(1));
                }
            }

            UUID contentId = null;
            if (payload != null) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\"contentId\"\\s*:\\s*\"([0-9a-fA-F\\-]+)\"").matcher(payload);
                if (matcher.find()) {
                    contentId = UUID.fromString(matcher.group(1));
                }
            }

            if (sagaId != null && contentId != null) {
                log.warn("[WatchingSession] Extracted sagaId [{}] and contentId [{}] from malformed payload. Sending immediate FAILED response.",
                        sagaId, contentId);
                sendSagaResponse(ContentDeletionSagaEvent.failed(sagaId, contentId, "WATCHING_SESSION", "DESERIALIZATION_FAILURE: " + errorMsg));
            } else {
                log.warn("[WatchingSession] Cannot extract identifiers from malformed payload (sagaId={}, contentId={}). Relying on timeout scheduler.",
                        sagaId, contentId);
            }
        } catch (Exception ex) {
            log.error("[WatchingSession] Failed during deserialization fallback handling", ex);
        }
    }

    private void processContentDeletionSaga(ContentDeletionSagaEvent event) {
        if ("COMPENSATE".equalsIgnoreCase(event.action())) {
            log.info("Received COMPENSATE action in watching-session. Live chat rooms / sessions do not require restoration. Skipping: {}", event.sagaId());
            return;
        }

        String inboxKey = "inbox:saga-start:" + event.sagaId() + ":watching-session";
        String prevStatus = redisTemplate.opsForValue().get(inboxKey);
        if (prevStatus != null) {
            log.info("[Inbox] Saga start already processed with status [{}] in watching-session. Resending response: {}", prevStatus, inboxKey);
            if ("FAILED".equalsIgnoreCase(prevStatus)) {
                sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "WATCHING_SESSION", "Previously failed"));
            } else {
                sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "WATCHING_SESSION"));
            }
            return;
        }

        try {
            // 해당 contentId와 연관된 시청 세션 정리
            watchingSessionCommandService.deleteByLiveChatRoomId(event.contentId());

            // Redis Inbox에 성공 처리 기록 (TTL 7일)
            redisTemplate.opsForValue().set(inboxKey, "SUCCESS", Duration.ofDays(7));

            // Saga 성공 이벤트 응답 (Kafka를 통해 mopl-content로 전파)
            sendSagaResponse(ContentDeletionSagaEvent.success(event.sagaId(), event.contentId(), "WATCHING_SESSION"));
        } catch (Exception e) {
            log.error("Failed to delete watching session for contentId: {}", event.contentId(), e);
            // Redis Inbox에 실패 처리 기록 (TTL 7일)
            redisTemplate.opsForValue().set(inboxKey, "FAILED", Duration.ofDays(7));
            // 실패 응답 전송 (Kafka)
            sendSagaResponse(ContentDeletionSagaEvent.failed(event.sagaId(), event.contentId(), "WATCHING_SESSION", e.getMessage()));
            // 비즈니스 실패 통보 완료 후 정상 리턴하여 Kafka 재시도 에러 폭풍 차단
        }
    }

    private void sendSagaResponse(ContentDeletionSagaEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(RabbitConfig.ROUTING_KEY_SAGA_RESPONSE, event.sagaId().toString(), payload);
            log.info("[Kafka] Sent saga response from watching-session: {}", payload);
        } catch (Exception e) {
            log.error("[Kafka] Failed to send saga response from watching-session: {}", event, e);
        }
    }
}
