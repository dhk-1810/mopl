package org.codeit.sb06.team03.mopl.liveChatRoom.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.SendMessagePort;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendLiveChatRoomMessageQuery;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendPresenceMessageQuery;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SendMessageAdapter implements SendMessagePort {

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void broadcastPresenceMessage(SendPresenceMessageQuery sendPresenceMessageQuery) {
        UserSummary userSummary = sendPresenceMessageQuery.userSummary();

        WatchingSessionDto sessionDetails = new WatchingSessionDto(
                sendPresenceMessageQuery.watchingSessionId(),
                sendPresenceMessageQuery.watchingSessionCreatedAt(),
                userSummary
        );

        LiveChatRoomPresenceResponse response = new LiveChatRoomPresenceResponse(
                sendPresenceMessageQuery.type(),
                sessionDetails,
                sendPresenceMessageQuery.count()
        );

        messagingTemplate.convertAndSend(sendPresenceMessageQuery.destination(), response);
    }

    @Override
    public void broadcastLiveChatRoomMessage(SendLiveChatRoomMessageQuery sendLiveChatRoomMessageQuery) {
        UserSummary userSummary = sendLiveChatRoomMessageQuery.userSummary();
        String text = sendLiveChatRoomMessageQuery.text();
        String destination = sendLiveChatRoomMessageQuery.destination();

        LiveChatRoomMessageResponse response = new LiveChatRoomMessageResponse(userSummary, text);

        messagingTemplate.convertAndSend(destination, response);
    }
}


