package org.codeit.sb06.team03.mopl.playlist.application.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Playlist;
import org.springframework.data.domain.Slice;

public interface LoadPlaylistsPort {

    Slice<Playlist> findAllBy();
}
