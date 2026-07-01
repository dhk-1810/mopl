package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;

import java.util.UUID;

public interface CreateDMChatRoomUseCase {
    DMChatRoom create(UUID userId, CreateDMChatRoomCommand command);
}
