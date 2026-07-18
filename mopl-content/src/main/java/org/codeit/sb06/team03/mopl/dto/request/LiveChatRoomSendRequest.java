package org.codeit.sb06.team03.mopl.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LiveChatRoomSendRequest(
        @JsonProperty("content")
        String text
) {
}
