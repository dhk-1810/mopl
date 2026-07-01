package org.codeit.sb06.team03.mopl.dm.dmMessage.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.MessagePassUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.MessagePassPort;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DMMessagePassService implements MessagePassUseCase {

    private final MessagePassPort messagePassPort;

    @Override
    public void pass(UUID dmChatRoomId, UUID messageId, String content, Instant createdAt, UserSummary sender, UserSummary receiver) {
        messagePassPort.pass(dmChatRoomId, messageId, content, createdAt, sender, receiver);
    }
}


