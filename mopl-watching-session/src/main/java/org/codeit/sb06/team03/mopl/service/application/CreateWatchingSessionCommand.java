package org.codeit.sb06.team03.mopl.service.application;

import java.util.UUID;

public record CreateWatchingSessionCommand(
        UUID liveChatRoomId,
        UUID watcherId
) {
}
