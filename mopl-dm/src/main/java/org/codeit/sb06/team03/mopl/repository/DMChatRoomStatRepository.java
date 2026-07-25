package org.codeit.sb06.team03.mopl.repository;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.entity.DMChatRoomStat;
import org.codeit.sb06.team03.mopl.entity.QDMChatRoomStat;

import java.util.List;
import java.util.UUID;

public interface DMChatRoomStatRepository extends QuerydslJpaRepository<DMChatRoomStat, UUID> {

    default boolean isActive(UUID dmChatRoomId, UUID accountId) {
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;
        return selectFrom(stat)
                .where(
                        stat.dmChatRoom.id.eq(dmChatRoomId),
                        stat.accountId.eq(accountId),
                        stat.activity.isTrue()
                )
                .fetchFirst() != null;
    }

    default List<UUID> findDMChatRoomIdsByAccountId(UUID accountId) {
        QDMChatRoomStat stat = QDMChatRoomStat.dMChatRoomStat;
        return select(stat.dmChatRoom.id)
                .from(stat)
                .where(stat.accountId.eq(accountId))
                .fetch();
    }
}
