package org.codeit.sb06.team03.mopl.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.codeit.sb06.team03.mopl.ErrorResponse;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestNotificationDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseNotificationDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;

@Tag(name = "알림")
@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "401", description = "인증 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "권한 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public interface NotificationApi {

    @Operation(summary = "알림 목록 조회 (커서 페이지네이션)")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<CursorResponseNotificationDto> getNotifications(@ParameterObject CursorRequestNotificationDto request, @RequestHeader(value = "X-User-Id") UUID userId);

    @Operation(summary = "알림 읽음 처리")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> deleteNotification(UUID id, @RequestHeader(value = "X-User-Id") UUID userId);
}
