package org.codeit.sb06.team03.mopl.common;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionResponse(
        UUID id,
        Instant createdAt,
        UserSummaryDto watcher,
        ContentResult content
) {

}