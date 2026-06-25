package org.codeit.sb06.team03.mopl.liveChat.application.out.query;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

public record SendLiveChatMessageQuery(
        UserSummaryDto userSummaryDto,
        String text,
        String destination
) {
}


