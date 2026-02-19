package org.codeit.sb06.team03.mopl.notification.infra.in;

import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto(
        List<Object> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}
