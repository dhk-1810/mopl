package org.codeit.sb06.team03.mopl.watchingSession.application.out;

import org.codeit.sb06.team03.mopl.watchingSession.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface LoadWatchingSessionPort {

    boolean existsByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId);

    Optional<WatchingSession> findByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId);

    long countByContentId(UUID contentId);

    Optional<WatchingSessionReadModel> findReadModelByWatcherId(UUID watcherId);

    Optional<WatchingSessionReadModel> findReadModelByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId);

    Slice<WatchingSessionReadModel> findReadModelByContentId(WatchingSessionSearchCondition query);
}
