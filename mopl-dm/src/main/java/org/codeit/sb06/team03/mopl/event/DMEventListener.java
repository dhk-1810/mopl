package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.service.application.DMChatRoomQueryService;
import org.codeit.sb06.team03.mopl.service.application.DMMessagePassService;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import org.codeit.sb06.team03.mopl.UserSummary;
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
    public void handleMessageSent(DMEvent.MessageSentEvent event) {
        log.info("DMMessageEventListener handleMessageSent called for dmChatRoomId={}, messageId={}", event.getDmChatRoomId(), event.getMessageId());
        try {
            dmMessagePassService.pass(
                    event.getDmChatRoomId(),
                    event.getMessageId(),
                    event.getContent(),
                    event.getCreatedAt(),
                    event.getSender(),
                    event.getReceiver()
            );
            if (!dmChatRoomQueryService.isParticipantActive(event.getReceiverId(), event.getDmChatRoomId())) {
                UserSummary sender = event.getSender();
                DirectMessageDto dto = new DirectMessageDto(
                        event.getMessageId().toString(),
                        event.getDmChatRoomId().toString(),
                        event.getCreatedAt().toString(),
                        sender,
                        event.getReceiver(),
                        event.getContent()
                );
                log.info("Receiver is not active. Publishing NewMessageMarkEvent directly to RabbitMQ for receiverId={}", event.getReceiverId());
                
                DMEvent.NewMessageMarkEvent mqEvent = new DMEvent.NewMessageMarkEvent(
                        event.getReceiverId(),
                        sender.name(),
                        event.getContent(),
                        dto
                );

                rabbitTemplate.convertAndSend(
                        RabbitConfig.DM_EXCHANGE,
                        "dm.notification-required",
                        mqEvent
                );
            }
        } catch (Exception e) {
            log.error("DM WebSocket 전송 실패 - dmChatRoomId={}, messageId={}", event.getDmChatRoomId(), event.getMessageId(), e);
        }
    }
}