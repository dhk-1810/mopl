package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.GetDMChatRoomUseCase;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.LoadDMChatRoomPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.LoadDMChatRoomStatPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DMChatRoomQueryService implements GetDMChatRoomUseCase {

    private final LoadDMChatRoomPort loadDMChatRoomPort;
    private final LoadDMChatRoomStatPort loadDMChatRoomStatPort;

    @Override
    public List<DMChatRoom> findAll(
            UUID userId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    ) {
        boolean ascending = "ASC".equalsIgnoreCase(sortDirection);
        return loadDMChatRoomPort.findAll(userId, cursor, idAfter, limit, ascending, sortBy);
    }

    @Override
    public long countAll(UUID userId) {
        return loadDMChatRoomPort.count(userId);
    }

    @Override
    public DMChatRoom findById(UUID userId, UUID dmChatRoomId) {
        return loadDMChatRoomPort.findById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));
    }

    @Override
    public DMChatRoom findByWith(UUID userId, UUID withUserId) {
        return loadDMChatRoomPort.findByParticipants(userId, withUserId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(withUserId));
    }

    @Override
    public boolean isParticipantActive(UUID userId, UUID dmChatRoomId) {
        return loadDMChatRoomStatPort.isActive(dmChatRoomId, userId);
    }
}
