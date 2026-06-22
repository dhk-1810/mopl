package org.codeit.sb06.team03.mopl.liveChat.infra.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;

public record LiveChatMessageResponse(
        UserSummaryDto sender,
        String content
) {
}