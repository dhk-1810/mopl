package org.codeit.sb06.team03.mopl.dto.response;

import java.util.List;
import java.util.UUID;

public record CursorResponseNotificationDto(
        List<NotificationDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        String sortDirection
) {
}
