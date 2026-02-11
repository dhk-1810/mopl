package org.codeit.sb06.team03.mopl.playlist.infra.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistDto(
        UUID id,
        UserSummaryDto owner,
        String title,
        String description,
        Instant updatedAt,
        long subscriberCount,
        boolean subscribedByMe
        //List<ContentDto> contents // TODO
) {
}
