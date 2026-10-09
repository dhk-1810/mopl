package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.dto.response.LiveChatRoomMessageResponse;
import org.codeit.sb06.team03.mopl.dto.response.LiveChatRoomPresenceResponse;
import org.codeit.sb06.team03.mopl.dto.response.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.repository.WatchingSessionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveChatRoomCommandService {

    private final WatchingSessionRepository watchingSessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendPresenceMessage(UUID liveChatRoomId, SendPresenceMessageCommand command) {
        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());

        WatchingSessionDto sessionDetails = new WatchingSessionDto(
                command.watchingSessionId(),
                command.watchingSessionCreatedAt(),
                userSummary
        );

        long watcherCount = watchingSessionRepository.countByContentId(liveChatRoomId);

        LiveChatRoomPresenceResponse response = new LiveChatRoomPresenceResponse(
                command.type(),
                sessionDetails,
                watcherCount
        );

        messagingTemplate.convertAndSend(command.destination(), response);
    }

    public void sendLiveChatRoomMessage(SendLiveChatRoomMessageCommand command) {
        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());
        LiveChatRoomMessageResponse response = new LiveChatRoomMessageResponse(userSummary, command.content());
        messagingTemplate.convertAndSend(command.destination(), response);
    }
}
