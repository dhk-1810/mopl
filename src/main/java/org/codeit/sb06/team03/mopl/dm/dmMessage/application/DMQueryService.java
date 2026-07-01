package org.codeit.sb06.team03.mopl.dm.dmMessage.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.GetDMUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadDMMessagePort;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DMQueryService implements GetDMUseCase {

    private final LoadDMMessagePort loadDMMessagePort;

    @Override
    public List<DMMessage> findAll(
            UUID dmChatRoomId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    ) {
        boolean ascending = "ASC".equalsIgnoreCase(sortDirection);
        return loadDMMessagePort.findAll(dmChatRoomId, cursor, idAfter, limit, ascending, sortBy);
    }

    @Override
    public long countAll(UUID dmChatRoomId) {
        return loadDMMessagePort.count(dmChatRoomId);
    }

    @Override
    public Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId) {
        return loadDMMessagePort.findLatestByDMChatRoomId(dmChatRoomId);
    }

    @Override
    public Map<UUID, DMMessage> findLatestByDMChatRoomIds(Set<UUID> dmChatRoomIds) {
        return dmChatRoomIds.stream()
                .map(id -> Map.entry(id, loadDMMessagePort.findLatestByDMChatRoomId(id)))
                .filter(e -> e.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
    }
}