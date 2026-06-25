package org.codeit.sb06.team03.mopl.watchingSession.application.in;

import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetWatchingSessionUseCase {

    WatchingSessionReadModel get(UUID watcherId);

    Slice<WatchingSessionReadModel> get(UUID contentId, List<UUID> watcherIds, CursorWatchingSessionRequest request);

    WatchingSession get(UUID liveChatId, UUID watcherId);

    long countWatchersByContentId(UUID contentId);

    Map<UUID, Long> countWatchersByContentIds(List<UUID> contentIds);
}
