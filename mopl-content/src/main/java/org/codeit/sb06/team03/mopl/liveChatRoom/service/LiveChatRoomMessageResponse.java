package org.codeit.sb06.team03.mopl.liveChatRoom.service;

import org.codeit.sb06.team03.mopl.UserSummary;

public record LiveChatRoomMessageResponse(
        UserSummary sender,
        String content
) {
}