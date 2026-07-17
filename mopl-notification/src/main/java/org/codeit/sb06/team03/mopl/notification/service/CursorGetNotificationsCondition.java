package org.codeit.sb06.team03.mopl.notification.service;

import java.util.UUID;

public record CursorGetNotificationsCondition(
        UUID receiverId,
        String cursor,
        UUID idAfter,
        int limit,
        String sortBy,
        boolean descending
) {
}
