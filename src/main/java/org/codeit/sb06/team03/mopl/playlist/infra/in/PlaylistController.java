package org.codeit.sb06.team03.mopl.playlist.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.bff.BffPlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/playlist")
public class PlaylistController implements PlaylistApi {

    private final BffPlaylistService bffPlaylistService;

    @Override
    @PostMapping
    public ResponseEntity<PlaylistDto> postPlaylist(
            @RequestBody @Valid PlaylistCreateRequest request
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        PlaylistDto playlistDto = bffPlaylistService.createPlaylist(request, UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorResponsePlaylistDto> getPlaylists(
            @ModelAttribute CursorRequestPlaylistDto request
    ) {
        CursorResponsePlaylistDto response = bffPlaylistService.getPlaylists(request);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> getPlaylist(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        PlaylistDto playlistDto = bffPlaylistService.getPlaylist(playlistId, UUID.randomUUID()); // 조회자 ID
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @PatchMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> patchPlaylist(
            @PathVariable String playlistId,
            @RequestBody PlaylistUpdateRequest request
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        PlaylistDto playlistDto = bffPlaylistService.updatePlayList(playlistId, request, UUID.randomUUID());
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        bffPlaylistService.deletePlaylist(playlistId, UUID.randomUUID());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> postSubscription(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        bffPlaylistService.subscribePlaylist(playlistId, UUID.randomUUID());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable String playlistId
//            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        bffPlaylistService.unsubscribePlaylist(playlistId, UUID.randomUUID());
        return ResponseEntity.noContent().build();
    }
}
