package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.enums.NotificationLevel;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
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
    public void handleDMNotificationRequired(DMEvent event) {
        log.info("Received DMNotificationRequiredEvent from RabbitMQ: {}", event);
        NotificationDto notificationDto = notificationCommandService.create(
                event.receiverId(),
                "[DM]" + event.senderName(),
                event.content(),
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME_NOTIFICATION, event.receiverId());
    }
}
