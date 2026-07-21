package org.codeit.sb06.team03.mopl.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestPlaylistDto;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.PlaylistUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponsePlaylistDto;
import org.codeit.sb06.team03.mopl.dto.response.PlaylistDto;
import org.codeit.sb06.team03.mopl.service.composite.PlaylistCompositeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        PlaylistDto playlistDto = playlistCompositeService.createPlaylist(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorResponsePlaylistDto> getPlaylists(
            @ModelAttribute CursorRequestPlaylistDto request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    ) {
        CursorResponsePlaylistDto response = playlistCompositeService.getAll(request, userId);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> getPlaylist(
            @PathVariable UUID playlistId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    ) {
        PlaylistDto playlistDto = playlistCompositeService.get(playlistId, userId); // 조회자 ID
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @PatchMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> patchPlaylist(
            @PathVariable UUID playlistId,
            @RequestBody PlaylistUpdateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        PlaylistDto playlistDto = playlistCompositeService.updatePlayList(playlistId, request, userId);
        return ResponseEntity.ok(playlistDto);
    }

    @Override
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        playlistCompositeService.deletePlaylist(playlistId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> postCuration(
            @PathVariable UUID playlistId,
            @PathVariable UUID contentId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        playlistCompositeService.addContentToPlaylist(playlistId, contentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{playlistId}/contents/{contentId}")
    public ResponseEntity<Void> deleteCuration(
            @PathVariable UUID playlistId,
            @PathVariable UUID contentId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        playlistCompositeService.deleteContentFromPlaylist(playlistId, contentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> postSubscription(
            @PathVariable UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        playlistCompositeService.subscribePlaylist(playlistId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{playlistId}/subscription")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable UUID playlistId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        playlistCompositeService.unsubscribePlaylist(playlistId, userId);
        return ResponseEntity.noContent().build();
    }
}
