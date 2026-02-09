package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.SavePlaylistPort;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SavePlaylistAdapter implements SavePlaylistPort {

    private final PlaylistRepository repository;

    @Override
    public void save(Playlist playlist) {
        repository.save(playlist);
    }
}
