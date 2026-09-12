package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record MessagePassedEvent(
        UUID messageId,
        UUID dmChatRoomId,
        UUID receiverId,
        String content
) {}
