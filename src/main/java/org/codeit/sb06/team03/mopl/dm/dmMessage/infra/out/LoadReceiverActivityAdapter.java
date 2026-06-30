package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.GetDMChatRoomUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadReceiverActivityPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadReceiverActivityAdapter implements LoadReceiverActivityPort {

    private final GetDMChatRoomUseCase getDMChatRoomUseCase;

    @Override
    public boolean isReceiverActive(UUID receiverId, UUID dmChatRoomId) {
        return getDMChatRoomUseCase.isParticipantActive(receiverId, dmChatRoomId);
    }
}
