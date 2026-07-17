package org.codeit.sb06.team03.mopl.notification.controller;

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
