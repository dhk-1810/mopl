package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.DirectMessageDto;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DMMessagePassService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void pass(UUID dmChatRoomId, UUID messageId, String content, Instant createdAt, UserSummary sender, UserSummary receiver) {
        DirectMessageDto dto = new DirectMessageDto(
                messageId.toString(),
                dmChatRoomId.toString(),
                createdAt.toString(),
                sender,
                receiver,
                content
        );
        String destination = "/sub/conversations/" + dmChatRoomId + "/direct-messages";
        messagingTemplate.convertAndSend(destination, dto);
    }
}


