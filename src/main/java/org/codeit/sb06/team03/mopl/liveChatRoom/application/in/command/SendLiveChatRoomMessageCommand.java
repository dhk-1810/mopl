package org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command;

import java.util.UUID;

public record SendLiveChatRoomMessageCommand(
        UUID accountId,
        String name,
        String profileImageUrl,
        String text,
        String destination
) {
}
