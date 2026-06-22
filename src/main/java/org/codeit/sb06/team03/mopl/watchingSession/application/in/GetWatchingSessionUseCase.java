package org.codeit.sb06.team03.mopl.watchingSession.application.in;

import org.codeit.sb06.team03.mopl.content.infra.in.WatchingSessionCursorRequest;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface GetWatchingSessionUseCase {

    Slice<WatchingSessionReadModel> get(UUID watcherId);

    Slice<WatchingSessionReadModel> get(UUID contentId, WatchingSessionCursorRequest request);

    WatchingSession get(UUID liveChatId, UUID watcherId);

    long countByContentId(UUID contentId);
}
