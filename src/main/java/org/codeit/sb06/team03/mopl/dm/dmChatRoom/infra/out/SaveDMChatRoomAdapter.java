package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.SaveDMChatRoomPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SaveDMChatRoomAdapter implements SaveDMChatRoomPort {

    private final DMChatRoomRepository repository;

    @Override
    public DMChatRoom save(DMChatRoom dmChatRoom) {
        return repository.save(dmChatRoom);
    }
}
