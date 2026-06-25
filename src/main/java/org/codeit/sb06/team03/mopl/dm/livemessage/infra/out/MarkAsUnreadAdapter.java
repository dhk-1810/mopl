package org.codeit.sb06.team03.mopl.dm.livemessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.conversation.application.in.ReadMessageUseCase;
import org.codeit.sb06.team03.mopl.dm.livemessage.application.out.MarkAsUnreadPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MarkAsUnreadAdapter implements MarkAsUnreadPort {

    private final ReadMessageUseCase messageReadUseCase;

    @Override
    public void markAsUnread(UUID conversationId, UUID receiverId) {
        messageReadUseCase.markAsUnread(conversationId, receiverId);
    }
}
