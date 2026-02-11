package org.codeit.sb06.team03.mopl.playlist.infra.in;

import java.time.Instant;
import java.util.UUID;

public record PlaylistData (
        UUID id,
        UUID ownerId,
        String title,
        String description,
        Instant updatedAt,
        long subscriberCount
) {
}
