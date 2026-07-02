package org.codeit.sb06.team03.mopl.watchingSession.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionCommand;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.DeleteWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.DeleteWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.SaveWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionDuplicateException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class WatchingSessionCommandService implements
        CreateWatchingSessionUseCase, DeleteWatchingSessionUseCase {

    private final SaveWatchingSessionPort saveWatchingSessionPort;
    private final LoadWatchingSessionPort loadWatchingSessionPort;
    private final DeleteWatchingSessionPort deleteWatchingSessionPort;

    @Override
    @Transactional("watchingSessionTransactionManager")
    public void create(CreateWatchingSessionCommand command) {
        UUID liveChatRoomId = command.liveChatRoomId();
        UUID watcherId = command.watcherId();

        if (loadWatchingSessionPort.existsByLiveChatRoomIdAndWatcherId(liveChatRoomId, watcherId)) {
            throw WatchingSessionDuplicateException.fromLiveChatRoomIdAndAccountId(liveChatRoomId, watcherId);
        }

        WatchingSession watchingSession = WatchingSession.create(watcherId, liveChatRoomId);
        saveWatchingSessionPort.save(watchingSession);
    }

    @Override
    @Transactional("watchingSessionTransactionManager")
    public void deleteByWatcherId(UUID watcherId) {
        deleteWatchingSessionPort.deleteByWatcherId(watcherId);
    }

    @Override
    @Transactional("watchingSessionTransactionManager")
    public void delete(UUID id) {
        deleteWatchingSessionPort.deleteById(id);
    }

}
