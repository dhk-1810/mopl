package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.dto.UserSummary;

public record LiveChatRoomMessageResponse(
        UserSummary sender,
        String content
) {
}