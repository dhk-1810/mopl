package org.codeit.sb06.team03.mopl.notification.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.notification.application.in.CreateNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.infra.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.sse.application.SseUseCase;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DMNotificationConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final SseUseCase sseUseCase;

    private static final String EVENT_NAME_NOTIFICATION = "notifications";

    @RabbitListener(queues = RabbitConfig.DM_NOTIFICATION_REQUIRED_QUEUE)
    public void handleDMNotificationRequired(DMNotificationRequiredEvent event) {
        log.info("Received DMNotificationRequiredEvent from RabbitMQ: {}", event);
        NotificationDto notificationDto = createNotificationUseCase.create(
                event.receiverId(),
                "[DM]" + event.senderName(),
                event.content(),
                NotificationLevel.INFO
        );
        sseUseCase.send(notificationDto, EVENT_NAME_NOTIFICATION, event.receiverId());
    }
}
