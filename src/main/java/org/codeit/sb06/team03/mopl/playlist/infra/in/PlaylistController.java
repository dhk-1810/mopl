package org.codeit.sb06.team03.mopl.playlist.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.bff.BffPlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/playlist")
public class PlaylistController implements PlaylistApi {

    private final BffPlaylistService bffPlaylistService;

    @Override
    @PostMapping
    public ResponseEntity<PlaylistDto> postPlaylists(
            @RequestBody @Valid PlaylistCreateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        // TODO id 추출 후 전달
        PlaylistDto playlistDto = bffPlaylistService.createPlaylist(request, );
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistDto);
    }

}
