package org.codeit.sb06.team03.mopl.dm.livemessage.domain;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LiveMessageService {

    public LiveMessage create(UUID conversationId, UUID senderId, UUID receiverId, String content, UserSummary sender, UserSummary receiver) {
        return LiveMessage.create(conversationId, senderId, receiverId, content, sender, receiver);
    }
}