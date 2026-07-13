package org.codeit.sb06.team03.mopl.watchingSession.application.in;

import org.codeit.sb06.team03.mopl.UserSummary;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
        UUID id,
        Instant createdAt,
        UserSummary watcher
) {

}
