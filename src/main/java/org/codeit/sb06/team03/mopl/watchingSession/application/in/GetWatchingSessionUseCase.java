package org.codeit.sb06.team03.mopl.watchingSession.application.in;

import jakarta.annotation.Nullable;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface GetWatchingSessionUseCase {

    @Nullable
    WatchingSessionReadModel getByContentId(UUID watcherId);

    WatchingSessionReadModel get(UUID liveChatId, UUID watcherId);

    Slice<WatchingSessionReadModel> getByContentId(UUID contentId, List<UUID> watcherIds, CursorWatchingSessionRequest request);

    long countWatchersByContentId(UUID contentId); // 엔터티 필드로 대체됨.

    Map<UUID, Long> countWatchersByContentIds(List<UUID> contentIds); // 엔터티 필드로 대체됨.
}
