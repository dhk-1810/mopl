package org.codeit.sb06.team03.mopl.dm.dmMessage.service;

import java.util.UUID;

public record MessageReceiveCommand(
        UUID messageId,
        UUID dmChatRoomId
) {
}
