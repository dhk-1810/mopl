package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.entity.LiveChatRoom;
import org.codeit.sb06.team03.mopl.exception.LiveChatRoomDuplicateException;
import org.codeit.sb06.team03.mopl.repository.LiveChatRoomRepository;
import org.codeit.sb06.team03.mopl.dto.response.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.dto.response.LiveChatRoomMessageResponse;
import org.codeit.sb06.team03.mopl.dto.response.LiveChatRoomPresenceResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(value = "contentTransactionManager", readOnly = true)
@RequiredArgsConstructor
public class LiveChatRoomCommandService {

    private final LiveChatRoomRepository liveChatRoomRepository;
    private final ContentRepository contentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void sendPresenceMessage(UUID liveChatRoomId, SendPresenceMessageCommand command) {
        UUID contentId = liveChatRoomId; // LiveChatRoom과 Content는 같은 ID를 쓰고 있음

        ContentReadModel readModel = contentRepository.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));

        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());

        WatchingSessionDto sessionDetails = new WatchingSessionDto(
                command.watchingSessionId(),
                command.watchingSessionCreatedAt(),
                userSummary
        );

        LiveChatRoomPresenceResponse response = new LiveChatRoomPresenceResponse(
                command.type(),
                sessionDetails,
                readModel.watcherCount()
        );

        messagingTemplate.convertAndSend(command.destination(), response);
    }

    public void sendLiveChatRoomMessage(SendLiveChatRoomMessageCommand command) {
        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());
        LiveChatRoomMessageResponse response = new LiveChatRoomMessageResponse(userSummary, command.text());
        messagingTemplate.convertAndSend(command.destination(), response);
    }

    @Transactional("contentTransactionManager")
    public void create(UUID contentId) {
        if (liveChatRoomRepository.existsById(contentId)) {
            throw LiveChatRoomDuplicateException.fromId(contentId);
        }

        LiveChatRoom liveChatRoom = LiveChatRoom.create(contentId);
        liveChatRoomRepository.save(liveChatRoom);
    }

    @Transactional("contentTransactionManager")
    public void delete(UUID contentId) {
        liveChatRoomRepository.deleteById(contentId);
    }
}