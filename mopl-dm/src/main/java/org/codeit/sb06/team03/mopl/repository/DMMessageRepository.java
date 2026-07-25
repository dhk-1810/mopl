package org.codeit.sb06.team03.mopl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.codeit.sb06.team03.mopl.entity.QDMMessage;
import org.codeit.sb06.team03.mopl.util.CursorUtils;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DMMessageRepository extends QuerydslJpaRepository<DMMessage, UUID> {

    default List<DMMessage> findAll(
            UUID dmChatRoomId,
            @Nullable String cursor,
            @Nullable String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    ) {
        QDMMessage m = QDMMessage.dMMessage;

        BooleanExpression where = m.dmChatRoomId.eq(dmChatRoomId);
        BooleanExpression cursorCond = CursorUtils.buildCursorCondition(m.createdAt, m.id, cursor, idAfter, ascending);
        if (cursorCond != null) where = where.and(cursorCond);

        return select(m).from(m)
                .where(where)
                .orderBy(ascending ? m.createdAt.asc() : m.createdAt.desc(),
                        ascending ? m.id.asc() : m.id.desc())
                .limit(limit)
                .fetch();
    }

    default long count(UUID dmChatRoomId) {
        QDMMessage m = QDMMessage.dMMessage;
        Long result = select(m.count()).from(m)
                .where(m.dmChatRoomId.eq(dmChatRoomId))
                .fetchOne();
        return result == null ? 0L : result;
    }

    default Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId) {
        QDMMessage m = QDMMessage.dMMessage;
        return Optional.ofNullable(
                select(m).from(m)
                        .where(m.dmChatRoomId.eq(dmChatRoomId))
                        .orderBy(m.createdAt.desc(), m.id.desc())
                        .fetchFirst()
        );
    }
}
