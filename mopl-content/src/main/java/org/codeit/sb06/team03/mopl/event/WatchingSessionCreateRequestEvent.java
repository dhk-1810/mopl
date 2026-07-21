package org.codeit.sb06.team03.mopl.event;

import java.time.Instant;
import java.util.UUID;

public record WatchingSessionCreateRequestEvent(
        UUID sessionId,
        UUID contentId,
        UUID watcherId,
        Instant createdAt
) {
}
