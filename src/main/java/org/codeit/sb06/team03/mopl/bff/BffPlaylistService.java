package org.codeit.sb06.team03.mopl.bff;

import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistCreateRequest;
import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;

public interface BffPlaylistService {

    PlaylistDto createPlaylist(PlaylistCreateRequest request);
}
