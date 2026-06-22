package org.codeit.sb06.team03.mopl.common;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.time.Instant;
import java.util.UUID;

// TODO 뭐여이건
public record WatchingSessionResponse(
        UUID id,
        Instant createdAt,
        UserSummaryDto watcher,
        ContentReadModel content
) {

}