package org.codeit.sb06.team03.mopl.playlist.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.PlaylistCompositeService;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.PlaylistDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/playlists")
public class PlaylistController implements PlaylistApi {

    private final PlaylistCompositeService playlistCompositeService;

    @Override
    @PostMapping
    public ResponseEntity<PlaylistDto> postPlaylist(
            @RequestBody @Valid PlaylistCreateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        PlaylistDto playlistDto = playlistCompositeService.createPlaylist(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorResponsePlaylistDto> getPlaylists(
            @ModelAttribute CursorRequestPlaylistDto request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        UUID userId =  (user != null) ? user.getId() : null;
        CursorResponsePlaylistDto response = playlistCompositeService.getAll(request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> getPlaylist(
            @PathVariable UUID playlistId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        UUID userId =  (user != null) ? user.getId() : null;
        PlaylistDto playlistDto = playlistCompositeService.get(playlistId, user.getId()); // 조회자 ID
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @PatchMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> patchPlaylist(
            @PathVariable UUID playlistId,
            @RequestBody PlaylistUpdateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        PlaylistDto playlistDto = playlistCompositeService.updatePlayList(playlistId, request, user.getId());
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable UUID playlistId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        playlistCompositeService.deletePlaylist(playlistId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> postCuration(
            @PathVariable UUID playlistId,
            @PathVariable UUID contentId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        playlistCompositeService.addContentToPlaylist(playlistId, contentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> deleteCuration(
            @PathVariable UUID playlistId,
            @PathVariable UUID contentId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        playlistCompositeService.deleteContentFromPlaylist(playlistId, contentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> postSubscription(
            @PathVariable UUID playlistId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        playlistCompositeService.subscribePlaylist(playlistId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable UUID playlistId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        playlistCompositeService.unsubscribePlaylist(playlistId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
