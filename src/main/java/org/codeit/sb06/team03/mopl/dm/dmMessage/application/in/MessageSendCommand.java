package org.codeit.sb06.team03.mopl.dm.dmMessage.application.in;

import java.util.UUID;

public record MessageSendCommand(
        UUID dmChatRoomId,
        UUID senderId,
        UUID receiverId,
        String content
) {
}
