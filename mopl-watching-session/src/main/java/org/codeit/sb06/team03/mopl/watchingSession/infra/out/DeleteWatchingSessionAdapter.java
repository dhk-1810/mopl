package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.DeleteWatchingSessionPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Profile("!redis")
@Component
@RequiredArgsConstructor
public class DeleteWatchingSessionAdapter implements DeleteWatchingSessionPort {

    private final WatchingSessionRepository watchingSessionRepository;

    @Override
    public void deleteByWatcherId(UUID watcherId) {
        watchingSessionRepository.deleteByWatcherId(watcherId);
    }

    @Override
    public void deleteById(UUID id) {
        watchingSessionRepository.deleteById(id);
    }
}
