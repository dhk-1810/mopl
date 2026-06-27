package org.codeit.sb06.team03.mopl.liveChat.infra.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

public record LiveChatMessageResponse(
        UserSummary sender,
        String content
) {
}