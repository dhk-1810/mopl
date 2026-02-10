package org.codeit.sb06.team03.mopl.playlist.application.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;

import java.util.UUID;

public interface GetPlaylistUseCase {

    PlaylistDto get(String playlistId, UUID viewerId);

}
