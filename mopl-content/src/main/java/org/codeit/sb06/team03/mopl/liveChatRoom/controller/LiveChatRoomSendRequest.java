package org.codeit.sb06.team03.mopl.liveChatRoom.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LiveChatRoomSendRequest(
        @JsonProperty("content")
        String text
) {
}
