package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.dto.UserSummary;

public record DirectMessageDto(
        String id,
        String dmChatRoomId,
        String createdAt,
        UserSummary sender,
        UserSummary receiver,
        String content
) {
}
