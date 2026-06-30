package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in;


import io.swagger.v3.oas.annotations.media.Schema;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

public record DirectMessageDto(
        @Schema(description = "메시지 ID", format = "uuid")
        String id,

        @Schema(description = "대화 ID", format = "uuid")
        String dmChatRoomId,

        @Schema(description = "메시지 생성 시간", format = "date-time")
        String createdAt,

        @Schema(description = "발신자 정보")
        UserSummary sender,

        @Schema(description = "수신자 정보")
        UserSummary receiver,

        @Schema(description = "메시지 내용")
        String content
) {
}


