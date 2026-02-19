package org.codeit.sb06.team03.mopl.notification.infra.out;

import java.util.UUID;

public record CursorGetNotificationsCondition(
        UUID ownerId,
        String cursor,
        String idAfter,
        int limit,
        String sortBy,
        String sortDirection
) {
}
