package org.codeit.sb06.team03.mopl.dm.dmMessage.application.out;

import java.util.UUID;

public interface LoadReceiverActivityPort {
    boolean isReceiverActive(UUID dmChatRoomId, UUID receiverId);
}
