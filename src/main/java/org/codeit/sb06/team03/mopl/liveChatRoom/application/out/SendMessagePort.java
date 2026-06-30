package org.codeit.sb06.team03.mopl.liveChatRoom.application.out;

import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendLiveChatRoomMessageQuery;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query.SendPresenceMessageQuery;

public interface SendMessagePort {

    void broadcastPresenceMessage(SendPresenceMessageQuery sendPresenceMessageQuery);

    void broadcastLiveChatRoomMessage(SendLiveChatRoomMessageQuery sendLiveChatRoomMessageQuery);
}
