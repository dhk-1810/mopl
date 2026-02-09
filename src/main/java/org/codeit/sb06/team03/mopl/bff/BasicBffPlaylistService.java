package org.codeit.sb06.team03.mopl.bff;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistCommand;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BasicBffPlaylistService implements BffPlaylistService {

    private final PlaylistMapper playlistMapper;
    private final CreatePlaylistUseCase createPlaylistUseCase;

    @Override
    public PlaylistDto createPlaylist(PlaylistCreateRequest request) {
        CreatePlaylistCommand command = playlistMapper.toCommand(request);
        Playlist playlist = createPlaylistUseCase.create(command);
        return new PlaylistDto();
    }
}
