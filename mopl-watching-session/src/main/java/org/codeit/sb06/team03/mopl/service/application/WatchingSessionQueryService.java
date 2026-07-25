package org.codeit.sb06.team03.mopl.service.application;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.repository.redis.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.codeit.sb06.team03.mopl.dto.request.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.dto.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.exception.WatchingSessionNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WatchingSessionQueryService {

    private final WatchingSessionRepository watchingSessionRepository;

    @Nullable
    public WatchingSessionReadModel getByContentId(UUID watcherId) {
        return watchingSessionRepository.findReadModelByWatcherId(watcherId)
                .orElse(null);
    }

    public WatchingSessionReadModel get(UUID liveChatRoomId, UUID watcherId) {
        return watchingSessionRepository.findReadModelByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)
                .orElseThrow(() -> WatchingSessionNotFoundException.fromLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId));
    }

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

        return watchingSessionRepository.findReadModelByContentId(query);
    }

    public long countWatchersByContentId(UUID contentId) {
        return watchingSessionRepository.countByContentId(contentId);
    }

    public Map<UUID, Long> countWatchersByContentIds(List<UUID> contentIds) {
        return Map.of();
    }

}
