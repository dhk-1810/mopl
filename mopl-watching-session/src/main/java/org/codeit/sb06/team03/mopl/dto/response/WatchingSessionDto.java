package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
        UUID id,
        Instant createdAt,
        UserSummary watcher
) {

}
