package org.codeit.sb06.team03.mopl.command;

import java.util.UUID;

public record CreateDMChatRoomCommand(
        UUID withUserId
) {
}
