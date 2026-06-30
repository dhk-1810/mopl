package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;

public interface SaveDMChatRoomPort {
    DMChatRoom save(DMChatRoom dmChatRoom);
}
