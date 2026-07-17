package org.codeit.sb06.team03.mopl.dm.dmChatRoom.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.*;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoom;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.DMChatRoomService;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomAlreadyExistsException;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomNotFoundException;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.out.DMChatRoomRepository;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMMessageNotFoundException;
import org.codeit.sb06.team03.mopl.dm.dmMessage.domain.DMMessage;
import org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out.DMMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMChatRoomCommandService {

    private final DMChatRoomService dmChatRoomService;
    private final DMChatRoomRepository dmChatRoomRepository;
    private final DMMessageRepository dmMessageRepository;

    public DMChatRoom create(UUID userId, CreateDMChatRoomCommand command) {
        UUID withUserId = command.withUserId();
        dmChatRoomRepository.findByParticipants(userId, withUserId)
                .ifPresent(c -> { throw new DMChatRoomAlreadyExistsException(withUserId); });

        DMChatRoom dmChatRoom = dmChatRoomService.create(userId, withUserId);
        return dmChatRoomRepository.save(dmChatRoom);
    }

    public void read(ReadMessageCommand command) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.markAsRead(dmChatRoom, command.userId());
        dmChatRoomRepository.save(dmChatRoom);

        DMMessage message = dmMessageRepository.findById(command.directMessageId())
                .orElseThrow(() -> new DMMessageNotFoundException(command.directMessageId()));
        message.markAsRead();
        dmMessageRepository.save(message);
    }

    public void markAsUnread(UUID dmChatRoomId, UUID receiverId) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoomService.markAsUnread(dmChatRoom, receiverId);
        dmChatRoomRepository.save(dmChatRoom);
    }

    public void join(JoinDMMessageCommand command) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.joinDMMessage(dmChatRoom, command.userId());
        dmChatRoomRepository.save(dmChatRoom);
    }

    public void leave(LeaveDMMessageCommand command) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(command.dmChatRoomId())
                .orElseThrow(() -> new DMChatRoomNotFoundException(command.dmChatRoomId()));

        dmChatRoomService.leaveDMMessage(dmChatRoom, command.userId());
        dmChatRoomRepository.save(dmChatRoom);
    }
}
