package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import org.codeit.sb06.team03.mopl.common.WatchingSessionDto;

public record LiveChatRoomPresenceResponse(
        String type,
        WatchingSessionDto watchingSession,
        long watcherCount
) {
}
