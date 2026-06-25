package org.codeit.sb06.team03.mopl.liveChat.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.liveChat.application.out.SendMessagePort;
import org.codeit.sb06.team03.mopl.liveChat.application.out.query.SendLiveChatMessageQuery;
import org.codeit.sb06.team03.mopl.liveChat.application.out.query.SendPresenceMessageQuery;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendMessageAdapter implements SendMessagePort {

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void broadcastPresenceMessage(SendPresenceMessageQuery sendPresenceMessageQuery) {
        UserSummaryDto userSummary = sendPresenceMessageQuery.userSummary();
        ContentReadModel contentResult = sendPresenceMessageQuery.contentResult();

        WatchingSessionDto sessionDetails = new WatchingSessionDto(
                sendPresenceMessageQuery.watchingSessionId(),
                sendPresenceMessageQuery.watchingSessionCreatedAt(),
                userSummary,
                contentResult
        );

        LiveChatPresenceResponse response = new LiveChatPresenceResponse(
                sendPresenceMessageQuery.type(),
                sessionDetails,
                sendPresenceMessageQuery.count()
        );

        messagingTemplate.convertAndSend(sendPresenceMessageQuery.destination(), response);
    }

    @Override
    public void broadcastLiveChatMessage(SendLiveChatMessageQuery sendLiveChatMessageQuery) {
        UserSummaryDto userSummary = sendLiveChatMessageQuery.userSummaryDto();
        String text = sendLiveChatMessageQuery.text();
        String destination = sendLiveChatMessageQuery.destination();

        LiveChatMessageResponse response = new LiveChatMessageResponse(userSummary, text);

        messagingTemplate.convertAndSend(destination, response);
    }
}


