package org.codeit.sb06.team03.mopl.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.event.WatchingSessionEvent.WatchingSessionCreatedEvent;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSession extends AbstractAggregateRoot<WatchingSession> {

    private UUID id;

    @NotNull
    private UUID watcherId;

    @NotNull
    private UUID liveChatRoomId;

    @NotNull
    private Instant createdAt;

    private short version;

    public static WatchingSession create(UUID watcherId, UUID liveChatRoomId) {
        WatchingSession watchingSession = new WatchingSession();
        watchingSession.id = UUID.randomUUID();
        watchingSession.watcherId = watcherId;
        watchingSession.liveChatRoomId = liveChatRoomId;
        watchingSession.createdAt = Instant.now();
        watchingSession.registerEvent(new WatchingSessionCreatedEvent(watcherId, watchingSession.id));

        return watchingSession;
    }

    public static WatchingSession createWithId(UUID id, UUID watcherId, UUID liveChatRoomId, Instant createdAt) {
        WatchingSession watchingSession = new WatchingSession();
        watchingSession.id = id;
        watchingSession.watcherId = watcherId;
        watchingSession.liveChatRoomId = liveChatRoomId;
        watchingSession.createdAt = createdAt;
        watchingSession.registerEvent(new WatchingSessionCreatedEvent(watcherId, id));

        return watchingSession;
    }
}
