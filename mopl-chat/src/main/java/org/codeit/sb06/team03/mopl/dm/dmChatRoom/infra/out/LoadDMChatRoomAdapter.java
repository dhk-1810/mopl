package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.LoadDMChatRoomPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class LoadDMChatRoomAdapter implements LoadDMChatRoomPort {

    private final DMChatRoomRepository dmChatRoomRepository;

    @Override
    public List<DMChatRoom> findAll(
            UUID userId,
            String cursor,
            String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    ) {
        List<UUID> ids = dmChatRoomRepository.findAllIds(userId, cursor, idAfter, limit + 1, ascending, sortBy);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<DMChatRoom> dm_chat_rooms = dmChatRoomRepository.findAllByIds(ids);

        Map<UUID, DMChatRoom> map = dm_chat_rooms.stream()
                .collect(Collectors.toMap(DMChatRoom::getId, c -> c));
        return ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public long count(UUID userId) {
        return dmChatRoomRepository.count(userId);
    }

    @Override
    public Optional<DMChatRoom> findById(UUID dmChatRoomId) {
        return dmChatRoomRepository.findDMChatRoomById(dmChatRoomId);
    }

    @Override
    public Optional<DMChatRoom> findByParticipants(UUID userId, UUID withUserId) {
        return dmChatRoomRepository.findByParticipants(userId, withUserId);
    }
}
