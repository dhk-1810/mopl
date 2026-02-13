package org.codeit.sb06.team03.mopl.playlist.application.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistDto;

import java.util.UUID;

public interface UpdatePlaylistUseCase {

    PlaylistDto update(String playlistId, UpdatePlaylistCommand command, UUID ownerId);

}
