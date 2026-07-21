package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.DMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.DMChatRoomCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDMChatRoomDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.codeit.sb06.team03.mopl.ErrorResponse;
import org.codeit.sb06.team03.mopl.controller.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@Tag(name = "다이렉트 메시지")
@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "401", description = "인증 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "권한 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public interface DMApi {

    @Operation(summary = "대화 목록 조회(커서 페이지네이션)")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<CursorResponseDMChatRoomDto> getDMChatRooms(
            @ModelAttribute CursorRequestDMChatRoomDto request,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "대화 생성")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<DMChatRoomDto> createDMChatRoom(
            @RequestBody(required = true) @Valid DMChatRoomCreateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "DM 읽음 처리")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> readDirectMessage(
            @PathVariable UUID dmChatRoomId,
            @PathVariable UUID directMessageId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "대화 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "404", description = "해당 리소스 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<DMChatRoomDto> getDMChatRoom(
            @PathVariable UUID dmChatRoomId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "DM 목록 조회 (커서 페이지네이션)")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @PathVariable UUID dmChatRoomId,
            @ModelAttribute CursorRequestDirectMessageDto request,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "특정 사용자와의 대화 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "404", description = "해당 리소스 없음")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<DMChatRoomDto> getDMChatRoomWith(
            @Parameter(required = true) @RequestParam UUID userId,
            @RequestHeader(value = "X-User-Id") UUID currentUserId
    );
}
