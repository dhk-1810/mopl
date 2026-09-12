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

@Slf4j
@Component
@RequiredArgsConstructor
public class DMEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;

    private static final String EVENT_NAME_NOTIFICATION = "notifications";

    @RabbitListener(queues = RabbitConfig.DM_NOTIFICATION_REQUIRED_QUEUE)
    public void handleDMNotificationRequired(NewMessageMarkEvent event) {
        log.info("Received NewMessageMarkEvent from RabbitMQ: {}", event);
        if (event.receiverId() == null) {
            log.warn("NewMessageMarkEvent receiverId is null: {}", event);
            return;
        }
        NotificationDto notificationDto = notificationCommandService.create(
                event.receiverId(),
                "[DM]" + (event.senderName() != null ? event.senderName() : ""),
                event.content(),
                NotificationLevel.INFO
        );
        // 1. 알림 토스트/벨 아이콘용 SSE 전송
        if (notificationDto != null) {
            sseService.send(notificationDto, EVENT_NAME_NOTIFICATION, event.receiverId());
        }

        // 2. 실시간 DM 목록/안 읽은 배지 업데이트용 SSE 전송
        if (event.directMessage() != null) {
            sseService.send(event.directMessage(), "direct-messages", event.receiverId());
        }
    }
}
