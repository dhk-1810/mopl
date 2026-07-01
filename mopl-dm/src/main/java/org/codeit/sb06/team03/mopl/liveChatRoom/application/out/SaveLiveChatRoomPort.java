package org.codeit.sb06.team03.mopl.liveChatRoom.application.out;

import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;

public interface SaveLiveChatRoomPort {

    LiveChatRoom save(LiveChatRoom liveChatRoom);
}
