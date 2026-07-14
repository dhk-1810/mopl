package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Profile("!redis")
@Component
@RequiredArgsConstructor
public class LoadWatchingSessionAdapter implements LoadWatchingSessionPort {

    private final WatchingSessionRepository repository;

    @Override
    public boolean existsByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId) {
        return repository.existsByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId);
    }

    @Override
    public long countByContentId(UUID contentId) {
        return repository.countByLiveChatRoomId(contentId);
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByWatcherId(UUID watcherId) {
        return repository.findReadModelByWatcherId(watcherId);
    }

    @Override
    public Optional<WatchingSessionReadModel> findReadModelByLiveChatRoomIdAndWatcherId(UUID liveChatRoomId, UUID watcherId) {
        return repository.findReadModelByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId);
    }

    @Override
    public Slice<WatchingSessionReadModel> findReadModelByContentId(WatchingSessionSearchCondition condition) {
        return repository.findReadModelByContentId(condition);
    }
}
