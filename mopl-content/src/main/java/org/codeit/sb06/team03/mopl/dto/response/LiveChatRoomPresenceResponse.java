package org.codeit.sb06.team03.mopl.dto.response;



public record LiveChatRoomPresenceResponse(
        String type,
        WatchingSessionDto watchingSession,
        long watcherCount
) {
}
