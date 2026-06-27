package org.codeit.sb06.team03.mopl.watchingSession.application;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WatchingSessionQueryService implements GetWatchingSessionUseCase {

    private final LoadWatchingSessionPort loadWatchingSessionPort;

    @Override
    @Nullable
    public WatchingSessionReadModel getByContentId(UUID watcherId) {
        return loadWatchingSessionPort.findReadModelByWatcherId(watcherId)
                .orElse(null);
    }

    @Override
    public WatchingSessionReadModel get(UUID liveChatId, UUID watcherId) {
        return loadWatchingSessionPort.findReadModelByLiveChatIdAndWatcherId(liveChatId, watcherId)
                .orElseThrow(() -> WatchingSessionNotFoundException.fromLiveChatIdAndWatcherId(liveChatId, watcherId));
    }

    @Override
    public Slice<WatchingSessionReadModel> getByContentId(UUID contentId, List<UUID> watcherIds, CursorWatchingSessionRequest request) {
        Instant cursorInstant = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idAfterUuid = request.idAfter() != null ? UUID.fromString(request.idAfter()) : null;

        WatchingSessionSearchCondition query = new WatchingSessionSearchCondition(
                contentId,
                watcherIds,
                cursorInstant,
                idAfterUuid,
                request.limit(),
                request.sortDirection(),
                request.sortBy()
        );

        return loadWatchingSessionPort.findReadModelByContentId(query);
    }

    @Override
    public long countWatchersByContentId(UUID contentId) {
        return loadWatchingSessionPort.countByContentId(contentId);
    }

    @Override
    public Map<UUID, Long> countWatchersByContentIds(List<UUID> contentIds) {
        return Map.of(); // TODO
    }

}
