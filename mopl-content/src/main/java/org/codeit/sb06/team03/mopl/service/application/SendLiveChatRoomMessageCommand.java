package org.codeit.sb06.team03.mopl.service.application;

import java.util.UUID;

public record SendLiveChatRoomMessageCommand(
        UUID accountId,
        String name,
        String profileImageUrl,
        String text,
        String destination
) {
}
