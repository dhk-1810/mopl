package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistsPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LoadPlaylistsAdapter implements LoadPlaylistsPort {

    private final PlaylistRepository playlistRepository;

    @Override
    public Slice<Playlist> findAllBy() {
        return playlistRepository.findAllBy();
    }

}
