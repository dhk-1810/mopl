package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.DirectMessageDto;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.MessagePassPort;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MessagePassAdapter implements MessagePassPort{

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void pass(UUID dmChatRoomId, UUID messageId, String content, Instant createdAt, UserSummary sender, UserSummary receiver) {
        DirectMessageDto dto = new DirectMessageDto(
                messageId.toString(),
                dmChatRoomId.toString(),
                createdAt.toString(),
                sender,
                receiver,
                content
        );
        String destination = "/sub/dm_chat_rooms/" + dmChatRoomId + "/direct-messages";
        messagingTemplate.convertAndSend(destination, dto);
    }
}