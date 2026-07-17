package org.codeit.sb06.team03.mopl.dm.dmChatRoom.service;

import java.util.UUID;

public record CreateDMChatRoomCommand(
        UUID withUserId
) {
}
