package org.codeit.sb06.team03.mopl.liveChatRoom.application.out;

import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;

import java.util.Optional;
import java.util.UUID;

public interface LoadLiveChatRoomPort {

    Optional<LiveChatRoom> findById(UUID liveChatRoomId);

    boolean existsById(UUID liveChatRoomId);
}
