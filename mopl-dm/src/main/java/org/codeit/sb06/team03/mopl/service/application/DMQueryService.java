package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.DMMessage;
import org.codeit.sb06.team03.mopl.repository.DMMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(value = "dmTransactionManager", readOnly = true)
public class DMQueryService {

    private final DMMessageRepository dmMessageRepository;

    public List<DMMessage> findAll(
            UUID dmChatRoomId,
            String cursor,
            String idAfter,
            int limit,
            String sortDirection,
            String sortBy
    ) {
        boolean ascending = "ASC".equalsIgnoreCase(sortDirection);
        return dmMessageRepository.findAll(dmChatRoomId, cursor, idAfter, limit + 1, ascending, sortBy);
    }

    public long countAll(UUID dmChatRoomId) {
        return dmMessageRepository.countByDmChatRoomIdAndIsDeletedFalse(dmChatRoomId);
    }

    public Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId) {
        return dmMessageRepository.findFirstByDmChatRoomIdAndIsDeletedFalseOrderByCreatedAtDescIdDesc(dmChatRoomId);
    }

    public Map<UUID, DMMessage> findLatestByDMChatRoomIds(Set<UUID> dmChatRoomIds) {
        return dmChatRoomIds.stream()
                .map(id -> {
                    Optional<DMMessage> msg = dmMessageRepository.findFirstByDmChatRoomIdAndIsDeletedFalseOrderByCreatedAtDescIdDesc(id);
                    return msg.map(dmMessage -> Map.entry(id, dmMessage)).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}