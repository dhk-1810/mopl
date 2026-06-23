package org.codeit.sb06.team03.mopl.watchingSession.infra.out;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionSearchCondition;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.codeit.sb06.team03.mopl.user.domain.QProfile.profile;
import static org.codeit.sb06.team03.mopl.watchingSession.domain.QWatchingSession.watchingSession;

public interface WatchingSessionRepository extends QuerydslJpaRepository<WatchingSession, UUID> {

    boolean existsByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    void deleteByWatcherId(UUID watcherId);

    Optional<WatchingSession> findByLiveChatIdAndWatcherId(UUID liveChatId, UUID watcherId);

    int countByLiveChatId(UUID liveChatId);

    long countByContentId(UUID contentId);

    Optional<WatchingSession> findByWatcherId(UUID watcherId);

    default Slice<WatchingSession> findByContentId(WatchingSessionSearchCondition condition) {
        OrderSpecifier<Instant> primaryOrder = getPrimaryOrder(condition.sortDirection());
        OrderSpecifier<UUID> secondaryOrder = getSecondaryOrder(condition.sortDirection());

        BooleanExpression cursorAndAssistanceCursorCondition =
                getCursorAndAssistanceCursorCondition(condition.cursor(), condition.idAfter(), condition.sortDirection());

        BooleanExpression watcherNameLikeCondition = getWatcherNameLikeCondition(condition.watcherNameLike());

        List<WatchingSession> sessions = select(watchingSession)
                .from(watchingSession)
                .innerJoin(profile).on(watchingSession.watcherId.eq(profile.accountId)) // TODO CQRS
                .where(
                        watchingSession.liveChatId.eq(condition.contentId()),
                        cursorAndAssistanceCursorCondition,
                        watcherNameLikeCondition
                )
                .limit(condition.limit() + 1)
                .orderBy(primaryOrder, secondaryOrder)
                .fetch();

        boolean hasNext = false;
        if (sessions.size() > condition.limit()) {
            sessions.remove(condition.limit());
            hasNext = true;
        }

        return new SliceImpl<>(sessions, PageRequest.ofSize(condition.limit()), hasNext);
    }

    private BooleanExpression getWatcherNameLikeCondition(String watcherName) {
        if (watcherName == null || watcherName.isBlank()) {
            return null;
        }
        return profile.name.like("%" + watcherName + "%");
    }

    private BooleanExpression getCursorAndAssistanceCursorCondition(
            Instant cursor, UUID assistanceCursor, String direction
    ) {
        if (cursor == null || assistanceCursor == null) {
            return null;
        }

        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.createdAt.after(cursor).or(
                    watchingSession.createdAt.eq(cursor).and(
                            watchingSession.id.gt(assistanceCursor)
                    )
            );
        } else {
            return watchingSession.createdAt.before(cursor).or(
                    watchingSession.createdAt.eq(cursor).and(
                            watchingSession.id.lt(assistanceCursor)
                    )
            );
        }
    }

    private OrderSpecifier<Instant> getPrimaryOrder(String direction) {
        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.createdAt.asc();
        } else {
            return watchingSession.createdAt.desc();
        }
    }

    private OrderSpecifier<UUID> getSecondaryOrder(String direction) {
        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.id.asc();
        } else {
            return watchingSession.id.desc();
        }
    }
}
