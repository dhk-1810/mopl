package org.codeit.sb06.team03.mopl.sse.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.sse.dto.NotificationSsePayload;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseRabbitListener {

    private final SseService sseService;

    // 알림 대상 사용자가 연결된 서버 인스턴스로 SSE 알림 전달
    @RabbitListener(queues = "#{@notificationInstanceQueue.name}")
    public void handleNotificationSse(NotificationSsePayload payload) {
        log.debug("Received targeted SSE payload for user {}: event={}", payload.receiverId(), payload.eventName());
        sseService.sendToLocalClient(payload.receiverId(), payload.eventName(), payload.data(), payload.eventId());
    }
}
