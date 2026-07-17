package org.codeit.sb06.team03.mopl.dm.dmChatRoom.service;

import java.util.UUID;

public record ReadMessageCommand(
        UUID dmChatRoomId,
        UUID directMessageId,
        UUID userId
) {
}
