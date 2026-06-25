package org.codeit.sb06.team03.mopl.liveChat.application.out.query;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

import java.time.Instant;
import java.util.UUID;

public record SendPresenceMessageQuery(
        UserSummaryDto userSummary,
        UUID watchingSessionId,
        Instant watchingSessionCreatedAt,
        int count,
        String type,
        String destination,
        ContentReadModel contentResult
) {
}


