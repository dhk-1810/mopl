package org.codeit.sb06.team03.mopl.watchingSession.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.domain.event.WatchingSessionEvent.WatchingSessionCreateEvent;
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
    private UUID liveChatId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private short version;

    public static WatchingSession create(UUID watcherId, UUID liveChatId) {
        WatchingSession watchingSession = new WatchingSession();
        watchingSession.id = UUID.randomUUID();
        watchingSession.watcherId = watcherId;
        watchingSession.liveChatId = liveChatId;
        watchingSession.createdAt = Instant.now();
        watchingSession.registerEvent(new WatchingSessionCreateEvent(watcherId, watchingSession.id));

        return watchingSession;
    }
}
