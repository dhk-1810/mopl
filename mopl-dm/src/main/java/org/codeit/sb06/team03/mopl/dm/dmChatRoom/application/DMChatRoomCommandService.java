package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.*;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.LoadDMChatRoomPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.out.SaveDMChatRoomPort;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoomService;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomAlreadyExistsException;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomNotFoundException;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.SaveDMMessagePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMChatRoomCommandService implements CreateDMChatRoomUseCase, ReadDMUseCase, JoinDMMessageUseCase, LeaveDMMessageUseCase {

    private final DMChatRoomService dmChatRoomService;
    private final LoadDMChatRoomPort loadDMChatRoomPort;
    private final SaveDMChatRoomPort saveDMChatRoomPort;
    private final SaveDMMessagePort saveDMMessagePort;

    @Override
    public DMChatRoom create(UUID userId, CreateDMChatRoomCommand command) {
        UUID withUserId = command.withUserId();
        loadDMChatRoomPort.findByParticipants(userId, withUserId)
                .ifPresent(c -> { throw new DMChatRoomAlreadyExistsException(withUserId); });

        DMChatRoom dmChatRoom = dmChatRoomService.create(userId, withUserId);
        return saveDMChatRoomPort.save(dmChatRoom);
    }

    @Override
    public void read(ReadMessageCommand command) {
        DMChatRoom dmChatRoom = loadDMChatRoomPort.findById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.markAsRead(dmChatRoom, command.userId());
        saveDMChatRoomPort.save(dmChatRoom);

        saveDMMessagePort.markAsRead(command.directMessageId());
    }

    @Override
    public void markAsUnread(UUID dmChatRoomId, UUID receiverId) {
        DMChatRoom dmChatRoom = loadDMChatRoomPort.findById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoomService.markAsUnread(dmChatRoom, receiverId);
        saveDMChatRoomPort.save(dmChatRoom);
    }

    @Override
    public void join(JoinDMMessageCommand command) {
        DMChatRoom dmChatRoom = loadDMChatRoomPort.findById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.joinDMMessage(dmChatRoom, command.userId());
        saveDMChatRoomPort.save(dmChatRoom);
    }

    @Override
    public void leave(LeaveDMMessageCommand command) {
        DMChatRoom dmChatRoom = loadDMChatRoomPort.findById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.leaveDMMessage(dmChatRoom, command.userId());
        saveDMChatRoomPort.save(dmChatRoom);
    }
}
