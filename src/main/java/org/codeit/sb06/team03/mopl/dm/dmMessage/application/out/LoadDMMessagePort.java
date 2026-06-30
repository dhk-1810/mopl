package org.codeit.sb06.team03.mopl.dm.dmMessage.application.out;

import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadDMMessagePort {

    List<DMMessage> findAll(
            UUID dmChatRoomId,
            String cursor,
            String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    );

    long count(UUID dmChatRoomId);

    Optional<DMMessage> findById(UUID messageId);

    Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId);
}