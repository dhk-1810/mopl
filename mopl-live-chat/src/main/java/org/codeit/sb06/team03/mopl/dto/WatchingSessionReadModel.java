package org.codeit.sb06.team03.mopl.dto;

import org.codeit.sb06.team03.mopl.entity.WatchingSession;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionReadModel (
        UUID id,
        UUID watcherId,
        UUID liveChatRoomId,
        Instant createdAt
) {
    public static WatchingSessionReadModel from(WatchingSession watchingSession) {
        return new WatchingSessionReadModel(
                watchingSession.getId(),
                watchingSession.getWatcherId(),
                watchingSession.getLiveChatRoomId(),
                watchingSession.getCreatedAt()
        );
    }
}
