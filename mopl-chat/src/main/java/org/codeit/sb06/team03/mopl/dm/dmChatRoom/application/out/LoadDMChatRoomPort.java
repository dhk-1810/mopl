package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadDMChatRoomPort {

    List<DMChatRoom> findAll(
            UUID userId,
            String cursor,
            String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    );

    long count(UUID userId);

    Optional<DMChatRoom> findById(UUID dmChatRoomId);

    Optional<DMChatRoom> findByParticipants(UUID userId, UUID withUserId);
}