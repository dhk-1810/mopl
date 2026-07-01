package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in;

public interface ReadDMUseCase {

    void read(ReadMessageCommand command);

    void markAsUnread(java.util.UUID dmChatRoomId, java.util.UUID receiverId);
}
