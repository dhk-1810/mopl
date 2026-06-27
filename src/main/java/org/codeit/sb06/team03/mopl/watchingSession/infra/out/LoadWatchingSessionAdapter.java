package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadWatchingSessionAdapter implements LoadWatchingSessionPort {

    private final WatchingSessionRepository repository;

    @Override
    public boolean existsByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId) {
        return repository.existsByLiveChatIdAndWatcherId(liveChatId, watcherId);
    }

    @Override
    public Optional<WatchingSession> findByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId) {
        return repository.findByLiveChatIdAndWatcherId(liveChatId, watcherId);
    }

    @Override
    public long countByContentId(UUID contentId) {
        return repository.countByLiveChatId(contentId);
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByWatcherId(UUID watcherId) {
        return repository.findReadModelByWatcherId(watcherId);
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId) {
        return repository.findReadModelByLiveChatIdAndWatcherId(liveChatId, watcherId);
    }

    @Override
    public Slice<WatchingSessionReadModel> findReadModelByContentId(WatchingSessionSearchCondition condition) {
        return repository.findReadModelByContentId(condition);
    }
}
