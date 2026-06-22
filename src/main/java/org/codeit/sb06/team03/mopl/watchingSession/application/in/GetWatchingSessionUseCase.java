package org.codeit.sb06.team03.mopl.watchingSession.application.in;

import org.codeit.sb06.team03.mopl.content.infra.in.WatchingSessionCursorRequest;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;

import java.util.List;
import java.util.UUID;

public interface GetWatchingSessionUseCase {

    List<WatchingSession> get(UUID watcherId);

    List<WatchingSession> get(UUID contentId, WatchingSessionCursorRequest request);

    WatchingSession get(UUID liveChatId, UUID watcherId);

    long countByContentId(UUID contentId);
}
