package org.codeit.sb06.team03.mopl.liveChatRoom.service;

import org.codeit.sb06.team03.mopl.dto.response.WatchingSessionDto;

public record LiveChatRoomPresenceResponse(
        String type,
        WatchingSessionDto watchingSession,
        long watcherCount
) {
}
