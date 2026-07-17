package org.codeit.sb06.team03.mopl.repository;

import org.codeit.sb06.team03.mopl.domain.WatchingSession;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.repository.postgres.WatchingSessionSearchCondition;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface WatchingSessionRepository {

    WatchingSession save(WatchingSession watchingSession);

    boolean existsByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId);

    long countByContentId(UUID contentId);

    Optional<WatchingSessionReadModel> findReadModelByWatcherId(UUID watcherId);

    Optional<WatchingSessionReadModel> findReadModelByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId);

    Slice<WatchingSessionReadModel> findReadModelByContentId(WatchingSessionSearchCondition query);

    void deleteByWatcherId(UUID watcherId);

    void deleteById(UUID id);
}
