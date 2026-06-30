package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in;

import java.util.UUID;

public record LeaveDMMessageCommand(
        UUID dmChatRoomId,
        UUID userId
) {
}
