package org.codeit.sb06.team03.mopl.playlist.application.in;

import java.util.UUID;

public interface DeletePlaylistUseCase {

    void delete(UUID playlistId, UUID ownerId);
}
