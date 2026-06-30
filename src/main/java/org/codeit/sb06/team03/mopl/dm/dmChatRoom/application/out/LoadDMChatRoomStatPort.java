package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out;

import java.util.List;
import java.util.UUID;

public interface LoadDMChatRoomStatPort {

    boolean isActive(UUID dmChatRoomId, UUID accountId);

    List<UUID> findDMChatRoomIdsByAccountId(UUID accountId);
}
