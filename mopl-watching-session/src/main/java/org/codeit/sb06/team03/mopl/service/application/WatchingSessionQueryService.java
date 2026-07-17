package org.codeit.sb06.team03.mopl.service.application;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.repository.postgres.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.dto.request.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.repository.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.exception.WatchingSessionNotFoundException;
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
    public WatchingSessionReadModel get(UUID liveChatRoomId, UUID watcherId) {
        return loadWatchingSessionPort.findReadModelByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)
                .orElseThrow(() -> WatchingSessionNotFoundException.fromLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId));
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
        return Map.of();
    }

}
