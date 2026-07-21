package org.codeit.sb06.team03.mopl.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.event.WatchingSessionEvent.WatchingSessionCreatedEvent;
import org.springframework.data.domain.AbstractAggregateRoot;



import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "watching_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_watcher_id_live_chat_id",
                        columnNames = {"watcher_id", "live_chat_id"} // 한 유저가 같은 liveCHat에 참여 불가하게 제한.
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchingSession extends AbstractAggregateRoot<WatchingSession> {

    @Id
    private UUID id;

    @NotNull
    @Column(name = "watcher_id", nullable = false)
    private UUID watcherId;

    @NotNull
    @Column(name = "live_chat_id", nullable = false)
    private UUID liveChatRoomId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
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
