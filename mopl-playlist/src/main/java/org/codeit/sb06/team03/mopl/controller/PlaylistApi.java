package org.codeit.sb06.team03.mopl.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.codeit.sb06.team03.mopl.ErrorResponse;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.PlaylistDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;

@Tag(name = "플레이리스트 관리")
@ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "401", description = "인증 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "403", description = "권한 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
@ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public interface PlaylistApi {

    @Operation(summary = "플레이리스트 생성")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> postPlaylist(
            PlaylistCreateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트 목록 조회 (커서 페이지네이션)")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<CursorResponsePlaylistDto> getPlaylists(
            @ParameterObject CursorRequestPlaylistDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    );

    @Operation(summary = "플레이리스트 단건 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> getPlaylist(
            UUID playlistId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    );

    @Operation(summary = "플레이리스트 수정")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> patchPlaylist(
            UUID playlistId,
            PlaylistUpdateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트 삭제")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> deletePlaylist(
            UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트에 컨텐츠 추가")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> postCuration(
            UUID playlistId,
            UUID contentId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트에서 컨텐츠 삭제")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> deleteCuration(
            UUID playlistId,
            UUID contentId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트 구독")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> postSubscription(
            UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

    @Operation(summary = "플레이리스트 구독 취소")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> deleteSubscription(
            UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    );

}
