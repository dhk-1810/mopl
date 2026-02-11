package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadPlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadPlaylistAdapter implements LoadPlaylistPort {

    private final PlaylistRepository playlistRepository;

    @Override
    public Optional<Playlist> findById(UUID id) {
        return playlistRepository.findById(id);
    }
}
