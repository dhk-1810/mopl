package org.codeit.sb06.team03.mopl.liveChatRoom.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.CreateLiveChatRoomUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.DeleteLiveChatRoomUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.SendLiveChatRoomMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.SendPresenceMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command.SendLiveChatRoomMessageCommand;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.*;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendLiveChatRoomMessageQuery;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendPresenceMessageQuery;
import org.codeit.sb06.team03.mopl.liveChatRoom.domain.LiveChatRoom;
import org.codeit.sb06.team03.mopl.liveChatRoom.domain.exception.LiveChatRoomDuplicateException;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(value = "dmTransactionManager", readOnly = true)
@RequiredArgsConstructor
public class LiveChatRoomCommandService implements
        SendPresenceMessageUseCase,  CreateLiveChatRoomUseCase, DeleteLiveChatRoomUseCase, SendLiveChatRoomMessageUseCase {

    private final LoadLiveChatRoomPort loadLiveChatRoomPort;
    private final SendMessagePort sendMessagePort;
    private final SaveLiveChatRoomPort saveLiveChatRoomPort;
    private final DeleteLiveChatRoomPort deleteLiveChatRoomPort;
    private final LiveChatRoomWatchingSessionQueryPort liveChatRoomWatchingSessionQueryPort;
    private final LoadContentPort loadContentPort;

    @Override
    public void sendPresenceMessage(UUID liveChatRoomId, SendPresenceMessageCommand command) {
        UUID contentId = liveChatRoomId; // LiveChatRoom과 Content는 같은 ID를 쓰고 있음

        ContentReadModel readModel = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));

        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());

        long watcherCount = liveChatRoomWatchingSessionQueryPort.countByLiveChatRoomId(liveChatRoomId);

        SendPresenceMessageQuery sendPresenceMessageQuery =
                new SendPresenceMessageQuery(
                        userSummary,
                        command.watchingSessionId(),
                        command.watchingSessionCreatedAt(),
                        watcherCount,
                        command.type(),
                        command.destination(),
                        readModel
                );

        sendMessagePort.broadcastPresenceMessage(sendPresenceMessageQuery);
    }

    @Override
    public void sendLiveChatRoomMessage(SendLiveChatRoomMessageCommand command) {
        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());
        String text = command.text();
        SendLiveChatRoomMessageQuery sendLiveChatRoomMessageQuery = new SendLiveChatRoomMessageQuery(userSummary, text, command.destination());
        sendMessagePort.broadcastLiveChatRoomMessage(sendLiveChatRoomMessageQuery);
    }

    @Override
    @Transactional("dmTransactionManager")
    public void create(UUID contentId) {
        if (loadLiveChatRoomPort.existsById(contentId)) {
            throw LiveChatRoomDuplicateException.fromId(contentId);
        }

        LiveChatRoom liveChatRoom = LiveChatRoom.create(contentId);
        saveLiveChatRoomPort.save(liveChatRoom);
    }

    @Override
    @Transactional("dmTransactionManager")
    public void delete(UUID contentId) {
        deleteLiveChatRoomPort.deleteById(contentId);
    }
}