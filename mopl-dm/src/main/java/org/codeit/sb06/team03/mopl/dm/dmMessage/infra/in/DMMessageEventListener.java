package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.GetDMChatRoomUseCase;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.DirectMessageDto;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.MessagePassUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.event.DMMessageEvent;
import org.codeit.sb06.team03.mopl.notification.application.in.CreateNotificationUseCase;
import org.codeit.sb06.team03.mopl.notification.domain.NotificationLevel;
import org.codeit.sb06.team03.mopl.notification.infra.in.NotificationDto;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.sse.application.SseUseCase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class DMMessageEventListener {

    private final MessagePassUseCase messagePassUseCase;
    private final GetDMChatRoomUseCase getDMChatRoomUseCase;
    private final SseUseCase sseUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;

    private static final String EVENT_NAME_DM = "direct-messages";
    private static final String EVENT_NAME_NOTIFICATION = "notifications";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(DMMessageEvent.MessageSentEvent event) {
        log.info("DMMessageEventListener handleMessageSent called for dmChatRoomId={}, messageId={}", event.getDmChatRoomId(), event.getMessageId());
        try {
            messagePassUseCase.pass(
                    event.getDmChatRoomId(),
                    event.getMessageId(),
                    event.getContent(),
                    event.getCreatedAt(),
                    event.getSender(),
                    event.getReceiver()
            );
            if (!getDMChatRoomUseCase.isParticipantActive(event.getReceiverId(), event.getDmChatRoomId())) {
                UserSummary sender = event.getSender();
                DirectMessageDto dto = new DirectMessageDto(
                        event.getMessageId().toString(),
                        event.getDmChatRoomId().toString(),
                        event.getCreatedAt().toString(),
                        sender,
                        event.getReceiver(),
                        event.getContent()
                );
                sseUseCase.send(dto, EVENT_NAME_DM, event.getReceiverId());

                NotificationDto notificationDto = createNotificationUseCase.create(
                        event.getReceiverId(),
                        "[DM]" + sender.name(),
                        event.getContent(),
                        NotificationLevel.INFO
                );
                sseUseCase.send(notificationDto, EVENT_NAME_NOTIFICATION, event.getReceiverId());
            }
        } catch (Exception e) {
            log.error("DM WebSocket 전송 실패 - dmChatRoomId={}, messageId={}", event.getDmChatRoomId(), event.getMessageId(), e);
        }
    }
}