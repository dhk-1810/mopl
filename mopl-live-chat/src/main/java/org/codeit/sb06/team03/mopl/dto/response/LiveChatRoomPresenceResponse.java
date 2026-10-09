package org.codeit.sb06.team03.mopl.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LiveChatRoomPresenceResponse(
        String type,
        WatchingSessionDto session,
        long watcherCount
) {
    @JsonProperty("watchingSession")
    public WatchingSessionDto getWatchingSession() {
        return session;
    }
}
