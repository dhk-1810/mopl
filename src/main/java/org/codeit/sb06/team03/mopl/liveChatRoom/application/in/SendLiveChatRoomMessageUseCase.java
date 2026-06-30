package org.codeit.sb06.team03.mopl.liveChatRoom.application.in;

import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command.SendLiveChatRoomMessageCommand;

public interface SendLiveChatRoomMessageUseCase {

    void sendLiveChatRoomMessage(SendLiveChatRoomMessageCommand sendLiveChatRoomMessageCommand);
}
