package org.codeit.sb06.team03.mopl.sse.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.sse.dto.NotificationSsePayload;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseRedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationSsePayload payload = objectMapper.readValue(message.getBody(), NotificationSsePayload.class);
            log.debug("Received targeted Redis SSE payload for user {}: event={}", payload.receiverId(), payload.eventName());
            sseService.sendToLocalClient(payload.receiverId(), payload.eventName(), payload.data(), payload.eventId());
        } catch (IOException e) {
            log.error("Failed to deserialize Redis SSE payload", e);
        }
    }
}
