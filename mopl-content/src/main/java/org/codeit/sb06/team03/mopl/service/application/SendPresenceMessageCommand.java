package org.codeit.sb06.team03.mopl.service.application;

import java.time.Instant;
import java.util.UUID;

public record SendPresenceMessageCommand(
        UUID watchingSessionId,
        Instant watchingSessionCreatedAt,
        UUID accountId,
        String name,
        String profileImageUrl,
        String type,
        String destination
) {
}
