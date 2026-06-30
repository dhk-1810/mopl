package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMMessageNotFoundException;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.SaveDMMessagePort;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SaveDMMessageAdapter implements SaveDMMessagePort {

    private final DMMessageRepository dmMessageRepository;

    @Override
    public DMMessage save(DMMessage dmMessage) {
        return dmMessageRepository.save(dmMessage);
    }

    @Override
    public void markAsRead(UUID messageId) {
        DMMessage message = dmMessageRepository.findById(messageId)
                .orElseThrow(() -> new DMMessageNotFoundException(messageId));
        message.markAsRead();
        dmMessageRepository.save(message);
    }
}