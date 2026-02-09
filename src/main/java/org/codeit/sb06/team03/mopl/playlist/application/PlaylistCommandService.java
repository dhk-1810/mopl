package org.codeit.sb06.team03.mopl.playlist.application;

import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistCommand;
import org.codeit.sb06.team03.mopl.playlist.application.in.CreatePlaylistUseCase;
import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;
import org.springframework.transaction.annotation.Transactional;

public class PlaylistCommandService implements CreatePlaylistUseCase {

    @Transactional
    @Override
    public Playlist create(CreatePlaylistCommand command) {
        return null;
    }

}
