package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.DMChatRoomQueryService;
import org.codeit.sb06.team03.mopl.service.application.DMMessagePassService;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.service.SseService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class DMMessageEventListener {

    private final DMMessagePassService dmMessagePassService;
    private final DMChatRoomQueryService dmChatRoomQueryService;
    private final SseService sseService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String EVENT_NAME_DM = "direct-messages";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(DMMessageEvent.MessageSentEvent event) {
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
                sseService.send(dto, EVENT_NAME_DM, event.getReceiverId());

                log.info("Receiver is not active. Publishing DMNotificationRequiredEvent for receiverId={}", event.getReceiverId());
                eventPublisher.publishEvent(new DMNotificationRequiredEvent(
                        event.getReceiverId(),
                        sender.name(),
                        event.getContent()
                ));
            }
        } catch (Exception e) {
            log.error("DM WebSocket 전송 실패 - dmChatRoomId={}, messageId={}", event.getDmChatRoomId(), event.getMessageId(), e);
        }
    }
}