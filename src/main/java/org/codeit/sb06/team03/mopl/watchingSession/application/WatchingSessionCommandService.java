package org.codeit.sb06.team03.mopl.watchingSession.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionCommand;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.DeleteWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.DeleteWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.SaveWatchingSessionPort;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionDuplicateException;
import org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class WatchingSessionCommandService implements
        CreateWatchingSessionUseCase, DeleteWatchingSessionUseCase, GetWatchingSessionUseCase {

    private final SaveWatchingSessionPort saveWatchingSessionPort;
    private final LoadWatchingSessionPort loadWatchingSessionPort;
    private final DeleteWatchingSessionPort deleteWatchingSessionPort;

    @Override
    @Transactional
    public void create(CreateWatchingSessionCommand command) {
        UUID liveChatId = command.liveChatId();
        UUID watcherId = command.watcherId();

        if (loadWatchingSessionPort.existsByLiveChatIdAndWatcherId(liveChatId, watcherId)) {
            throw WatchingSessionDuplicateException.fromLiveChatIdAndAccountId(liveChatId, watcherId);
        }

        WatchingSession watchingSession = WatchingSession.create(watcherId, liveChatId);
        saveWatchingSessionPort.save(watchingSession);
    }

    @Override
    @Transactional
    public void deleteByWatcherId(UUID watcherId) {
        deleteWatchingSessionPort.deleteByWatcherId(watcherId);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        deleteWatchingSessionPort.deleteById(id);
    }

    @Override
    public WatchingSessionReadModel get(UUID watcherId) {
//        Slice<WatchingSession> watchingSession = loadWatchingSessionPort.findByWatcherId(watcherId);
        WatchingSession watchingSession = loadWatchingSessionPort.findByWatcherId(watcherId)
                .orElseThrow(() -> WatchingSessionNotFoundException.fromWatcherId(watcherId));
        return WatchingSessionReadModel.from(watchingSession);
    }

    @Override
    public Slice<WatchingSessionReadModel> get(UUID contentId, CursorWatchingSessionRequest request) {
        Instant cursorInstant = request.cursor() != null ? Instant.parse(request.cursor()) : null;
        UUID idAfterUuid = request.idAfter() != null ? UUID.fromString(request.idAfter()) : null;

        WatchingSessionSearchCondition query = new WatchingSessionSearchCondition(
                contentId,
                request.watcherNameLike(),
                cursorInstant,
                idAfterUuid,
                request.limit(),
                request.sortDirection(),
                request.sortBy()
        );

        Slice<WatchingSession> slice = loadWatchingSessionPort.findByContentId(query);

        return slice.map(WatchingSessionReadModel::from);
    }
    @Override
    public WatchingSession get(UUID liveChatId, UUID watcherId) {
        return loadWatchingSessionPort.findByLiveChatIdAndWatcherId(liveChatId, watcherId)
                .orElseThrow(() -> WatchingSessionNotFoundException.fromLiveChatIdAndWatcherId(liveChatId, watcherId));
    }

    @Override
    public long countByContentId(UUID contentId) {
        return loadWatchingSessionPort.countByContentId(contentId);
    }
}
