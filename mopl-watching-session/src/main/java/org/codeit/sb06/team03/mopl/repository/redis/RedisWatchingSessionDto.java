package org.codeit.sb06.team03.mopl.repository.redis;

import org.codeit.sb06.team03.mopl.domain.WatchingSession;

public record RedisWatchingSessionDto(
        String id,
        String watcherId,
        String liveChatRoomId,
        long createdAtEpochMilli
) {
    public static RedisWatchingSessionDto from(WatchingSession session) {
        return new RedisWatchingSessionDto(
                session.getId().toString(),
                session.getWatcherId().toString(),
                session.getLiveChatRoomId().toString(),
                session.getCreatedAt().toEpochMilli()
        );
    }
}
