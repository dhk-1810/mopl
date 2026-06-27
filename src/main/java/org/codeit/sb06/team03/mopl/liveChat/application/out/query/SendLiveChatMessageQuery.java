package org.codeit.sb06.team03.mopl.liveChat.application.out.query;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

public record SendLiveChatMessageQuery(
        UserSummary userSummary,
        String text,
        String destination
) {
}


