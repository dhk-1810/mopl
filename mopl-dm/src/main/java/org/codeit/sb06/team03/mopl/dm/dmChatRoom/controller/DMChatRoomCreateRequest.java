package org.codeit.sb06.team03.mopl.dm.dmChatRoom.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DMChatRoomCreateRequest(
        @Schema(description = "대화 상대 정보 ID")
        @NotNull
        UUID withUserId
) {
}