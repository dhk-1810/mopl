package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import org.codeit.sb06.team03.mopl.UserSummary;

public record LiveChatRoomMessageResponse(
        UserSummary sender,
        String content
) {
}