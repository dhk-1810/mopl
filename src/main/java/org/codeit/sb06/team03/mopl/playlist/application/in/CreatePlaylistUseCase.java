package org.codeit.sb06.team03.mopl.playlist.application.in;

import org.codeit.sb06.team03.mopl.playlist.domain.Playlist;

public interface CreatePlaylistUseCase {

    Playlist create(CreatePlaylistCommand command);

}
