package org.codeit.sb06.team03.mopl.event;

import org.codeit.sb06.team03.mopl.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record MessageSentEvent(
        UUID messageId,
        UUID dmChatRoomId,
        UUID senderId,
        UUID receiverId,
        String content,
        Instant createdAt,
        UserSummary sender,
        UserSummary receiver
) {}
