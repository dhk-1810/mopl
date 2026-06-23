package org.codeit.sb06.team03.mopl.common;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionDto(
        UUID id,
        Instant createdAt,
        UserSummaryDto watcher,
        ContentReadModel content
) {

}