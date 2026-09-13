package org.codeit.sb06.team03.mopl.sse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.sse.NotificationInstanceId;
import org.codeit.sb06.team03.mopl.sse.dto.NotificationSsePayload;
import org.codeit.sb06.team03.mopl.sse.repository.NotificationSessionRepository;
import org.codeit.sb06.team03.mopl.sse.repository.SseRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class SseService {

    private final SseRepository sseRepository;
    private final NotificationSessionRepository sessionRedisRepository;
    private final NotificationInstanceId instanceId;
    private final RabbitTemplate rabbitTemplate;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 30; // 30분

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 연결 종료/타임아웃 시 리포지토리 및 Redis에서 삭제
        emitter.onCompletion(() -> handleDisconnect(emitter, receiverId));
        emitter.onTimeout(() -> handleDisconnect(emitter, receiverId));
        emitter.onError((e) -> handleDisconnect(emitter, receiverId));

        sseRepository.saveEmitter(emitter, receiverId);
        sessionRedisRepository.addSession(receiverId, instanceId.getId());

        // 연결되면 더미 이벤트 전송, 연결 확인
        ping(emitter, receiverId, "connect check");

        // 마지막으로 받은 메시지 이후 유실된 메시지 재전송
        if (lastEventId != null) {
            List<SseMessage> missedMessages = sseRepository.findAllMissedMessageByUserIdAndIdAfter(receiverId, lastEventId);
            missedMessages.forEach(message ->
                    sendToClient(emitter, receiverId, message.eventName(), message.data(), message.id().toString())
            );
        }

        return emitter;
    }

    private void handleDisconnect(SseEmitter emitter, UUID receiverId) {
        sseRepository.deleteEmitter(emitter, receiverId);
        if (sseRepository.findEmittersByUserId(receiverId).isEmpty()) {
            sessionRedisRepository.removeSession(receiverId, instanceId.getId());
        }
    }

    @Scheduled(fixedDelay = 15000) // 15초마다 실행
    public void sendHeartbeat() {

        Set<UUID> connectedUsers = sseRepository.findAllConnectedUserIds();
        if (connectedUsers.isEmpty()) return;

        Map<UUID, List<SseEmitter>> allEmitters = sseRepository.findAllEmittersByUserIdIn(connectedUsers);

        allEmitters.forEach((userId, emitters) -> {
            emitters.forEach(emitter -> ping(emitter, userId, "send heartbeat"));
            sessionRedisRepository.refreshSessionTtl(userId);
        });
        log.debug("Sent SSE heartbeat to {} users", connectedUsers.size());
    }

    public void send(Object data, String eventName, UUID receiverId) {
        SseMessage sseMessage = SseMessage.create(eventName, data);
        sseRepository.saveMessage(sseMessage, receiverId);

        Set<String> targetInstances = sessionRedisRepository.findInstanceIds(receiverId);

        // 로컬에 연결이 있거나 Redis에 현재 인스턴스가 등록되어 있는 경우 로컬 전송
        if (targetInstances.contains(instanceId.getId()) || !sseRepository.findEmittersByUserId(receiverId).isEmpty()) {
            sendToLocalClient(receiverId, eventName, data, sseMessage.id().toString());
        }

        // 다른 인스턴스들에 연결된 경우 RabbitMQ를 통해 해당 인스턴스로 라우팅
        for (String targetInstanceId : targetInstances) {
            if (!targetInstanceId.equals(instanceId.getId())) {
                sendToRemoteInstance(targetInstanceId, receiverId, eventName, data, sseMessage.id().toString());
            }
        }
    }

    public void sendAll(Map<UUID, Object> objectMap, String eventName) {
        if (objectMap.isEmpty()) return;

        Map<UUID, SseMessage> sseMessageMap = objectMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> SseMessage.create(eventName, entry.getValue())
                ));
        sseRepository.saveAllMessages(sseMessageMap);

        objectMap.forEach((userId, data) -> {
            SseMessage message = sseMessageMap.get(userId);
            String eventId = (message != null) ? message.id().toString() : UUID.randomUUID().toString();

            Set<String> targetInstances = sessionRedisRepository.findInstanceIds(userId);
            if (targetInstances.contains(instanceId.getId()) || !sseRepository.findEmittersByUserId(userId).isEmpty()) {
                sendToLocalClient(userId, eventName, data, eventId);
            }

            for (String targetInstanceId : targetInstances) {
                if (!targetInstanceId.equals(instanceId.getId())) {
                    sendToRemoteInstance(targetInstanceId, userId, eventName, data, eventId);
                }
            }
        });
    }

    private void sendToRemoteInstance(String targetInstanceId, UUID receiverId, String eventName, Object data, String eventId) {
        try {
            NotificationSsePayload payload = new NotificationSsePayload(receiverId, eventName, data, eventId);
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_SSE_EXCHANGE,
                    "notification.instance." + targetInstanceId,
                    payload
            );
        } catch (Exception e) {
            log.error("Failed to forward SSE notification to instance {}: {}", targetInstanceId, e.getMessage(), e);
        }
    }

    public void sendToLocalClient(UUID receiverId, String eventName, Object data, String eventId) {
        List<SseEmitter> emitters = sseRepository.findEmittersByUserId(receiverId);
        emitters.forEach(emitter -> sendToClient(emitter, receiverId, eventName, data, eventId));
    }

    // SseEmitter 객체를 통해 접속중인 모든 사용자에게 이벤트를 전송
    public void broadcast(String eventName, Object data) {
        log.info("Broadcasting event: {}", eventName);
    }

    // 만료된 SseEmitter 삭제
    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        log.info("SSE Emitter clean up task started.");

        Set<UUID> userIds = sseRepository.findAllConnectedUserIds();

        int removedCount = 0;
        for (UUID userId : userIds) {
            List<SseEmitter> emitters = sseRepository.findEmittersByUserId(userId);

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("cleanup-ping").data("check"));
                } catch (Exception e) {
                    handleDisconnect(emitter, userId);
                    removedCount++;
                }
            }
        }

        log.info("SSE Emitter clean up task finished. Removed {} zombie emitters.", removedCount);
    }

    /**
     * 헬퍼 메서드
     */

    private void ping(SseEmitter sseEmitter, UUID userId, String message) {
        sendToClient(sseEmitter, userId, "ping", message, null);
    }

    private void sendToClient(SseEmitter emitter, UUID userId, String eventName, Object data, String eventId) {
        try {
            SseEmitter.SseEventBuilder eventBuilder = SseEmitter.event()
                    .name(eventName)
                    .data(data);
            if (eventId != null && !eventId.isBlank()) {
                eventBuilder.id(eventId);
            }

            emitter.send(eventBuilder);

        } catch (IOException e) {
            handleDisconnect(emitter, userId);
            log.error("SSE send failed. removing connection: {}", userId);
        }
    }
}