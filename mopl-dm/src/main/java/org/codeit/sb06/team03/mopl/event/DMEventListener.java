package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import org.codeit.sb06.team03.mopl.service.application.DMChatRoomQueryService;
import org.codeit.sb06.team03.mopl.service.application.DMMessagePassService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class DMEventListener {

    private final DMMessagePassService dmMessagePassService;
    private final DMChatRoomQueryService dmChatRoomQueryService;
    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(MessageSentEvent event) {
        log.info("DMMessageEventListener handleMessageSent called for dmChatRoomId={}, messageId={}", event.dmChatRoomId(), event.messageId());
        try {
            dmMessagePassService.pass(
                    event.dmChatRoomId(),
                    event.messageId(),
                    event.content(),
                    event.createdAt(),
                    event.sender(),
                    event.receiver()
            );
            if (!dmChatRoomQueryService.isParticipantActive(event.receiverId(), event.dmChatRoomId())) {
                UserSummary sender = event.sender();
                DirectMessageDto dto = new DirectMessageDto(
                        event.messageId().toString(),
                        event.dmChatRoomId().toString(),
                        event.createdAt().toString(),
                        sender,
                        event.receiver(),
                        event.content()
                );
                log.info("Receiver is not active. Publishing NewMessageMarkEvent directly to RabbitMQ for receiverId={}", event.receiverId());
                
                NewMessageMarkEvent mqEvent = new NewMessageMarkEvent(
                        event.receiverId(),
                        sender.name(),
                        event.content(),
                        dto
                );

                rabbitTemplate.convertAndSend(
                        RabbitConfig.DM_EXCHANGE,
                        "dm.notification-required",
                        mqEvent
                );
            }
        } catch (Exception e) {
            log.error("DM WebSocket 전송 실패 - dmChatRoomId={}, messageId={}", event.dmChatRoomId(), event.messageId(), e);
        }
    }
}
