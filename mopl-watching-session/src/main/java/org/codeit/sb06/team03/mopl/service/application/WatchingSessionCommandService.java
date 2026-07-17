package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.codeit.sb06.team03.mopl.domain.WatchingSession;
import org.codeit.sb06.team03.mopl.exception.WatchingSessionDuplicateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WatchingSessionCommandService {

    private final WatchingSessionRepository watchingSessionRepository;

    @Transactional("watchingSessionTransactionManager")
    public void create(CreateWatchingSessionCommand command) {
        UUID liveChatRoomId = command.liveChatRoomId();
        UUID watcherId = command.watcherId();

        if (watchingSessionRepository.existsByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)) {
            throw WatchingSessionDuplicateException.fromLiveChatRoomIdAndAccountId(liveChatRoomId, watcherId);
        }

        WatchingSession watchingSession = WatchingSession.create(watcherId, liveChatRoomId);
        watchingSessionRepository.save(watchingSession);
    }

    @Transactional("watchingSessionTransactionManager")
    public void deleteByWatcherId(UUID watcherId) {
        watchingSessionRepository.deleteByWatcherId(watcherId);
    }

    @Transactional("watchingSessionTransactionManager")
    public void delete(UUID id) {
        watchingSessionRepository.deleteById(id);
    }

}
