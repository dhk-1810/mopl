package org.codeit.sb06.team03.mopl.command;

import java.util.UUID;

public record ReadMessageCommand(
        UUID dmChatRoomId,
        UUID directMessageId,
        UUID userId
) {
}
