package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadDMMessagePort;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadDMMessageAdapter implements LoadDMMessagePort {

    private final DMMessageRepository dmMessageRepository;

    @Override
    public List<DMMessage> findAll(
            UUID dmChatRoomId,
            String cursor,
            String idAfter,
            int limit,
            boolean ascending,
            String sortBy
    ) {
        return dmMessageRepository.findAll(dmChatRoomId, cursor, idAfter, limit + 1, ascending, sortBy);
    }

    @Override
    public long count(UUID dmChatRoomId) {
        return dmMessageRepository.count(dmChatRoomId);
    }

    @Override
    public Optional<DMMessage> findById(UUID messageId) {
        return dmMessageRepository.findById(messageId);
    }

    @Override
    public Optional<DMMessage> findLatestByDMChatRoomId(UUID dmChatRoomId) {
        return dmMessageRepository.findLatestByDMChatRoomId(dmChatRoomId);
    }
}
