package org.codeit.sb06.team03.mopl.playlist.infra.in;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "플레이리스트 관리")
public interface PlaylistApi {

    @Operation(summary = "플레이리스트 생성")
    @ApiResponse(responseCode = "201", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> postPlaylist(
            @RequestBody(required = true) @Valid PlaylistCreateRequest request
//            @AuthenticationPrincipal MoplUserDetails user
    );

    @Operation(summary = "플레이리스트 목록 조회 (커서 페이지네이션)")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> getPlaylists(
            @ModelAttribute CursorRequestPlaylistDto request
//            @AuthenticationPrincipal MoplUserDetails user
    );

    @Operation(summary = "플레이리스트 단건 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> getPlaylist(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    );

    @Operation(summary = "플레이리스트 수정")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<PlaylistDto> patchPlaylist(
            @PathVariable String playlistId,
            @RequestBody(required = true) PlaylistUpdateRequest request
//            @AuthenticationPrincipal MoplUserDetails user
    );

    @Operation(summary = "플레이리스트 삭제")
    @ApiResponse(responseCode = "204", description = "성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "401", description = "인증 오류")
    @ApiResponse(responseCode = "500", description = "서버 오류")
    ResponseEntity<Void> deletePlaylist(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    );

}
