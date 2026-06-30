package org.codeit.sb06.team03.mopl.dm.dmMessage.domain;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DMMessageService {

    public DMMessage create(UUID dmChatRoomId, UUID senderId, UUID receiverId, String content, UserSummary sender, UserSummary receiver) {
        return DMMessage.create(dmChatRoomId, senderId, receiverId, content, sender, receiver);
    }
}