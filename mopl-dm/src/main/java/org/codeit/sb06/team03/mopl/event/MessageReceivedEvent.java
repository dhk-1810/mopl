package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record MessageReceivedEvent(
        UUID messageId,
        UUID dmChatRoomId,
        UUID senderId,
        UUID receiverId
) {}
