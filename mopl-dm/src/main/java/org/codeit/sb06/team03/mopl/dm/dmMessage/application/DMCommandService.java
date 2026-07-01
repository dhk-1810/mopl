package org.codeit.sb06.team03.mopl.dm.dmMessage.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadLiveDMUserPort;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.MessageSendCommand;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.in.SendDMUseCase;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.MarkAsUnreadPort;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.SaveDMMessagePort;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMCommandService implements SendDMUseCase {

    private final DMService dmMessageService;
    private final SaveDMMessagePort saveDMMessagePort;
    private final LoadLiveDMUserPort loadDMUserPort;
    private final MarkAsUnreadPort markAsUnreadPort;

    @Override
    public DMMessage send(MessageSendCommand command) {
        UserSummary sender = loadDMUserPort.findByUserId(command.senderId());
        UserSummary receiver = loadDMUserPort.findByUserId(command.receiverId());

        DMMessage message = dmMessageService.create(
                command.dmChatRoomId(), command.senderId(),
                command.receiverId(), command.content(),
                sender, receiver
        );
        DMMessage savedMessage = saveDMMessagePort.save(message);

        markAsUnreadPort.markAsUnread(command.dmChatRoomId(), command.receiverId());

        return savedMessage;
    }
}