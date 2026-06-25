package org.codeit.sb06.team03.mopl.dm.conversation.application.in;

import java.util.UUID;

public record ReadMessageCommand(
        UUID conversationId,
        UUID directMessageId,
        UUID userId
) {
}
