package org.codeit.sb06.team03.mopl.watchingSession.application.out;

import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface LoadWatchingSessionPort {

    boolean existsByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    Optional<WatchingSession> findByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    long countByContentId(UUID contentId);

    Optional<WatchingSession> findByWatcherId(UUID watcherId);

    Slice<WatchingSession> findByContentId(WatchingSessionSearchCondition query);
}
