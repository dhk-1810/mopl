package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.notification.service.NotificationCommandService;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.controller.NotificationDto;
import org.codeit.sb06.team03.mopl.notification.controller.DMNotificationRequiredEvent;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DMNotificationConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;

    private static final String EVENT_NAME_NOTIFICATION = "notifications";

    @RabbitListener(queues = RabbitConfig.DM_NOTIFICATION_REQUIRED_QUEUE)
    public void handleDMNotificationRequired(DMNotificationRequiredEvent event) {
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
