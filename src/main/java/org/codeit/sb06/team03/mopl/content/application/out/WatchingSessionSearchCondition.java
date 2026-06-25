package org.codeit.sb06.team03.mopl.content.application.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WatchingSessionSearchCondition(
        UUID contentId,
        List<UUID> watcherIds,
        Instant cursor,
        UUID idAfter,
        int limit,
        String sortDirection,
        String sortBy
) {
}
