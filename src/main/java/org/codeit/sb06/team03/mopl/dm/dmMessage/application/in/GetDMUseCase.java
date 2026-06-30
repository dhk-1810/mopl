package org.codeit.sb06.team03.mopl.dm.dmMessage.application.in;

import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;

import java.util.*;

public interface GetDMUseCase {

    List<DMMessage> findAll(
            UUID dmChatRoomId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    );

    long countAll(UUID dmChatRoomId);

    Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId);

    Map<UUID, DMMessage> findLatestByDMChatRoomIds(Set<UUID> dmChatRoomIds);
}