package org.codeit.sb06.team03.mopl.liveChat.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.liveChat.application.in.CreateLiveChatUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.DeleteLiveChatUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.SendLiveChatMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.SendPresenceMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.command.SendLiveChatMessageCommand;
import org.codeit.sb06.team03.mopl.liveChat.application.in.command.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.liveChat.application.out.*;
import org.codeit.sb06.team03.mopl.liveChat.application.out.query.SendLiveChatMessageQuery;
import org.codeit.sb06.team03.mopl.liveChat.application.out.query.SendPresenceMessageQuery;
import org.codeit.sb06.team03.mopl.liveChat.domain.LiveChat;
import org.codeit.sb06.team03.mopl.liveChat.domain.exception.LiveChatDuplicateException;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LiveChatCommandService implements
        SendPresenceMessageUseCase,  CreateLiveChatUseCase, DeleteLiveChatUseCase, SendLiveChatMessageUseCase {

    private final LoadLiveChatPort loadLiveChatPort;
    private final SendMessagePort sendMessagePort;
    private final SaveLiveChatPort saveLiveChatPort;
    private final DeleteLiveChatPort deleteLiveChatPort;
    private final LiveChatWatchingSessionQueryPort liveChatWatchingSessionQueryPort;
    private final LoadContentPort loadContentPort;

    @Override
    public void sendPresenceMessage(UUID liveChatId, SendPresenceMessageCommand command) {
        UUID contentId = liveChatId; // LiveChat과 Content는 같은 ID를 쓰고 있음

        ContentReadModel readModel = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));

        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());

        long watcherCount = liveChatWatchingSessionQueryPort.countByLiveChatId(liveChatId);

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
    public void sendLiveChatMessage(SendLiveChatMessageCommand command) {
        UserSummary userSummary = new UserSummary(command.accountId(), command.name(), command.profileImageUrl());
        String text = command.text();
        SendLiveChatMessageQuery sendLiveChatMessageQuery = new SendLiveChatMessageQuery(userSummary, text, command.destination());
        sendMessagePort.broadcastLiveChatMessage(sendLiveChatMessageQuery);
    }

    @Override
    @Transactional
    public void create(UUID contentId) {
        if (loadLiveChatPort.existsById(contentId)) {
            throw LiveChatDuplicateException.fromId(contentId);
        }

        LiveChat liveChat = LiveChat.create(contentId);
        saveLiveChatPort.save(liveChat);
    }

    @Override
    @Transactional
    public void delete(UUID contentId) {
        deleteLiveChatPort.deleteById(contentId);
    }
}