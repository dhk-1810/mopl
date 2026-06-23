package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchingSessionRepository extends QuerydslJpaRepository<WatchingSession, UUID> {

    boolean existsByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    void deleteByWatcherId(UUID watcherId);

    Optional<WatchingSession> findByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    int countByLiveChatId(UUID liveChatId);

    long countByContentId(UUID contentId);

    Optional<WatchingSession> findByWatcherId(UUID watcherId);

    default Slice<WatchingSession> findByContentId(WatchingSessionSearchCondition condition) {
        return select()
                .from()
                .where()

    }
}
