package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;

import java.util.List;
import java.util.UUID;

public interface GetDMChatRoomUseCase {

    List<DMChatRoom> findAll(
            UUID userId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    );

    long countAll(UUID userId);

    DMChatRoom findById(UUID userId, UUID dmChatRoomId);

    DMChatRoom findByWith(UUID userId, UUID withUserId);

    boolean isParticipantActive(UUID userId, UUID dmChatRoomId);
}
