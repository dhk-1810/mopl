package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record WatchingSessionDeleteRequestEvent(
        UUID sessionId,
        UUID watcherId
) {
}
