package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.entity.DMChatRoom;
import org.codeit.sb06.team03.mopl.exception.DMChatRoomAlreadyExistsException;
import org.codeit.sb06.team03.mopl.exception.DMChatRoomCannotCreateWithSelfException;
import org.codeit.sb06.team03.mopl.exception.DMChatRoomNotFoundException;
import org.codeit.sb06.team03.mopl.repository.DMChatRoomRepository;
import org.codeit.sb06.team03.mopl.exception.DMMessageNotFoundException;
import org.codeit.sb06.team03.mopl.domain.entity.DMMessage;
import org.codeit.sb06.team03.mopl.repository.DMMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("dmTransactionManager")
public class DMChatRoomCommandService {

    private final DMChatRoomRepository dmChatRoomRepository;
    private final DMMessageRepository dmMessageRepository;

    public DMChatRoom create(UUID userId, UUID withUserId) {
        dmChatRoomRepository.findByParticipants(userId, withUserId)
                .ifPresent(c -> { throw new DMChatRoomAlreadyExistsException(withUserId); });

        if (userId.equals(withUserId)) {
            throw new DMChatRoomCannotCreateWithSelfException(userId);
        }
        DMChatRoom dmChatRoom = DMChatRoom.create(userId, withUserId);
        return dmChatRoomRepository.save(dmChatRoom);
    }

    public void read(UUID dmChatRoomId, UUID directMessageId, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoom.markAsRead(userId);
        dmChatRoomRepository.save(dmChatRoom);

        DMMessage message = dmMessageRepository.findById(directMessageId)
                .orElseThrow(() -> new DMMessageNotFoundException(directMessageId));
        message.markAsRead();
        dmMessageRepository.save(message);
    }

    public void markAsUnread(UUID dmChatRoomId, UUID receiverId) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoom.markAsUnread(receiverId);
        dmChatRoomRepository.save(dmChatRoom);
    }

    public void join(UUID dmChatRoomId, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoom.joinDMMessage(userId);
        dmChatRoomRepository.save(dmChatRoom);
    }

    public void leave(UUID dmChatRoomId, UUID userId) {
        DMChatRoom dmChatRoom = dmChatRoomRepository.findDMChatRoomById(dmChatRoomId)
                .orElseThrow(() -> new DMChatRoomNotFoundException(dmChatRoomId));

        dmChatRoom.leaveDMMessage(userId);
        dmChatRoomRepository.save(dmChatRoom);
    }
}
