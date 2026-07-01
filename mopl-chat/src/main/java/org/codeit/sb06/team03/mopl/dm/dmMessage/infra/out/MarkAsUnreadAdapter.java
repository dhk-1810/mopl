package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.ReadDMUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.MarkAsUnreadPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MarkAsUnreadAdapter implements MarkAsUnreadPort {

    private final ReadDMUseCase messageReadUseCase;

    @Override
    public void markAsUnread(UUID dmChatRoomId, UUID receiverId) {
        messageReadUseCase.markAsUnread(dmChatRoomId, receiverId);
    }
}
