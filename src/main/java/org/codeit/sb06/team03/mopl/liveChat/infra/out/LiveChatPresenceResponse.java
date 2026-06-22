package org.codeit.sb06.team03.mopl.liveChat.infra.out;

import org.codeit.sb06.team03.mopl.common.WatchingSessionResponse;

public record LiveChatPresenceResponse(
        String type,
        WatchingSessionResponse watchingSession,
        long watcherCount
) {
}
