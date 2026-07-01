package org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.exception.DMChatRoomCannotCreateWithSelfException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DMChatRoomService {

    public DMChatRoom create(UUID userId, UUID withUserId) {
        if (userId.equals(withUserId)) {
            throw new DMChatRoomCannotCreateWithSelfException(userId);
        }
        return DMChatRoom.create(userId, withUserId);
    }

    public void markAsRead(DMChatRoom dmChatRoom, UUID userId) {
        dmChatRoom.markAsRead(userId);
    }

    public void markAsUnread(DMChatRoom dmChatRoom, UUID userId) {
        dmChatRoom.markAsUnread(userId);
    }

    public void joinDMMessage(DMChatRoom dmChatRoom, UUID userId) {
        dmChatRoom.joinDMMessage(userId);
    }

    public void leaveDMMessage(DMChatRoom dmChatRoom, UUID userId) {
        dmChatRoom.leaveDMMessage(userId);
    }
}