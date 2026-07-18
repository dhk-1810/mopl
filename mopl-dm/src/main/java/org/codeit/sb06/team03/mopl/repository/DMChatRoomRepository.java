package org.codeit.sb06.team03.mopl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.CursorUtils;
import org.codeit.sb06.team03.mopl.domain.entity.DMChatRoom;
import org.codeit.sb06.team03.mopl.domain.entity.QDMChatRoom;
import org.codeit.sb06.team03.mopl.domain.entity.QDMChatRoomStat;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DMChatRoomRepository extends QuerydslJpaRepository<DMChatRoom, UUID> {

    default Optional<DMChatRoom> findByParticipants(UUID userId, UUID withUserId) {
        QDMChatRoom conv = QDMChatRoom.dMChatRoom;
        QDMChatRoomStat statA = new QDMChatRoomStat("statA");
        QDMChatRoomStat statB = new QDMChatRoomStat("statB");

        return Optional.ofNullable(
                select(conv).from(conv)
                        .join(statA).on(statA.dmChatRoom.eq(conv).and(statA.accountId.eq(userId)))
                        .join(statB).on(statB.dmChatRoom.eq(conv).and(statB.accountId.eq(withUserId)))
                        .leftJoin(conv.dmChatRoomStats).fetchJoin()
                        .fetchFirst()
        );
    }

    default List<UUID> findAllIds(
            UUID userId,
            @Nullable String cursor,
            @Nullable String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    ) {
        QDMChatRoom conv = QDMChatRoom.dMChatRoom;
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;

        BooleanExpression cursorCond = CursorUtils.buildCursorCondition(
                conv.createdAt, conv.id, cursor, idAfter, ascending
        );

        return select(conv.id).from(conv)
                .join(stat).on(stat.dmChatRoom.eq(conv).and(stat.accountId.eq(userId)))
                .where(cursorCond)
                .orderBy(ascending ? conv.createdAt.asc() : conv.createdAt.desc(),
                        ascending ? conv.id.asc() : conv.id.desc())
                .limit(limit)
                .fetch();
    }

    default List<DMChatRoom> findAllByIds(List<UUID> ids) {
        if (ids.isEmpty()) return List.of();

        QDMChatRoom conv = QDMChatRoom.dMChatRoom;
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;

        return select(conv).from(conv)
                .leftJoin(conv.dmChatRoomStats, stat).fetchJoin()
                .where(conv.id.in(ids))
                .fetch();
    }

    default long count(UUID userId) {
        QDMChatRoom conv = QDMChatRoom.dMChatRoom;
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;
        Long result = select(conv.count()).from(conv)
                .join(stat).on(stat.dmChatRoom.eq(conv).and(stat.accountId.eq(userId)))
                .fetchOne();
        return result == null ? 0L : result;
    }

    default Optional<DMChatRoom> findDMChatRoomById(UUID dmChatRoomId) {
        QDMChatRoom conv = QDMChatRoom.dMChatRoom;
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;
        return Optional.ofNullable(
                select(conv).from(conv)
                        .leftJoin(conv.dmChatRoomStats, stat).fetchJoin()
                        .where(conv.id.eq(dmChatRoomId))
                        .fetchFirst()
        );
    }
}
