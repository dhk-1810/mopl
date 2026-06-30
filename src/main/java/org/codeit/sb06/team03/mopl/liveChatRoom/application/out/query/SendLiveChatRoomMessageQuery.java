package org.codeit.sb06.team03.mopl.liveChatRoom.application.out.query;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

public record SendLiveChatRoomMessageQuery(
        UserSummary userSummary,
        String text,
        String destination
) {
}


