package org.codeit.sb06.team03.mopl.liveChatRoom.application.in;

import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command.SendPresenceMessageCommand;

import java.util.UUID;

public interface SendPresenceMessageUseCase {

    void sendPresenceMessage(
            UUID liveChatRoomId,
            SendPresenceMessageCommand sendPresenceMessageCommand
    );
}