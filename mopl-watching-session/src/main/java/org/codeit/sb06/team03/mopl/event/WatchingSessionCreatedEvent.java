package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record WatchingSessionCreatedEvent(
        UUID accountId,
        UUID watchingSessionId
) {}
