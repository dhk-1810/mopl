package org.codeit.sb06.team03.mopl.watchingSession.application.out;

import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadWatchingSessionPort {

    boolean existsByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    WatchingSession findByWatcherId(UUID watcherId);

    Optional<WatchingSession> findByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    long countByContentId(UUID contentId);
}
