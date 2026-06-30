package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.LoadDMChatRoomStatPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadDMChatRoomStatAdapter implements LoadDMChatRoomStatPort {

    private final DMChatRoomStatRepository dmChatRoomStatRepository;

    @Override
    public boolean isActive(UUID dmChatRoomId, UUID accountId) {
        return dmChatRoomStatRepository.isActive(dmChatRoomId, accountId);
    }

    @Override
    public List<UUID> findDMChatRoomIdsByAccountId(UUID accountId) {
        return dmChatRoomStatRepository.findDMChatRoomIdsByAccountId(accountId);
    }
}
