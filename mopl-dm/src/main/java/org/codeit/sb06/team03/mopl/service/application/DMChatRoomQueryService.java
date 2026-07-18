package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.entity.DMChatRoom;
import org.codeit.sb06.team03.mopl.exception.DMChatRoomNotFoundException;
import org.codeit.sb06.team03.mopl.repository.DMChatRoomRepository;
import org.codeit.sb06.team03.mopl.repository.DMChatRoomStatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(value = "dmTransactionManager", readOnly = true)
public class DMChatRoomQueryService {

    private final DMChatRoomRepository dmChatRoomRepository;
    private final DMChatRoomStatRepository dmChatRoomStatRepository;

    public List<DMChatRoom> findAll(
            UUID userId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    ) {
        boolean ascending = "ASC".equalsIgnoreCase(sortDirection);
        List<UUID> ids = dmChatRoomRepository.findAllIds(userId, cursor, idAfter, limit + 1, ascending, sortBy);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<DMChatRoom> dmChatRooms = dmChatRoomRepository.findAllByIds(ids);

        Map<UUID, DMChatRoom> map = dmChatRooms.stream()
                .collect(Collectors.toMap(DMChatRoom::getId, c -> c));
        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public long countAll(UUID userId) {
        return dmChatRoomRepository.count(userId);
    }

    public DMChatRoom findById(UUID userId, UUID dmChatRoomId) {
        return dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));
    }

    public DMChatRoom findByWith(UUID userId, UUID withUserId) {
        return dmChatRoomRepository.findByParticipants(userId, withUserId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(withUserId));
    }

    public boolean isParticipantActive(UUID userId, UUID dmChatRoomId) {
        return dmChatRoomStatRepository.isActive(dmChatRoomId, userId);
    }
}
