package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.event.AccountEvent;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.response.NotificationDto;
import org.codeit.sb06.team03.mopl.service.application.NotificationCommandService;
import org.codeit.sb06.team03.mopl.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.service.SseService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AccountEventConsumer {

    private final NotificationCommandService notificationCommandService;
    private final SseService sseService;

    private static final String EVENT_NAME = "notifications";

    @RabbitListener(queues = RabbitConfig.USER_ROLE_UPDATED_QUEUE)
    public void handleRoleUpdatedEvent(AccountEvent.RoleUpdatedEvent event) {
        log.info("Received RoleUpdatedEvent from RabbitMQ: {}", event);
        NotificationDto notificationDto = notificationCommandService.create(
                event.getAccountId(),
                "권한이 %s(으)로 변경되었어요.".formatted(event.getRole()),
                null,
                NotificationLevel.INFO
        );
        sseService.send(notificationDto, EVENT_NAME, event.getAccountId());
    }
}

