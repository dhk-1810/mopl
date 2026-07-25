package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.codeit.sb06.team03.mopl.entity.WatchingSession;
import org.codeit.sb06.team03.mopl.exception.WatchingSessionDuplicateException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WatchingSessionCommandService {

    private final WatchingSessionRepository watchingSessionRepository;

    public void create(CreateWatchingSessionCommand command) {
        UUID liveChatRoomId = command.liveChatRoomId();
        UUID watcherId = command.watcherId();

        if (watchingSessionRepository.existsByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)) {
            throw WatchingSessionDuplicateException.fromLiveChatRoomIdAndAccountId(liveChatRoomId, watcherId);
        }

        WatchingSession watchingSession = WatchingSession.create(watcherId, liveChatRoomId);
        watchingSessionRepository.save(watchingSession);
    }

    public void createWithId(UUID id, UUID liveChatRoomId, UUID watcherId, java.time.Instant createdAt) {
        if (watchingSessionRepository.existsByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)) {
            throw WatchingSessionDuplicateException.fromLiveChatRoomIdAndAccountId(liveChatRoomId, watcherId);
        }

        WatchingSession watchingSession = WatchingSession.createWithId(id, watcherId, liveChatRoomId, createdAt);
        watchingSessionRepository.save(watchingSession);
    }

    public void deleteByWatcherId(UUID watcherId) {
        watchingSessionRepository.deleteByWatcherId(watcherId);
    }

    public void delete(UUID id) {
        watchingSessionRepository.deleteById(id);
    }

}
