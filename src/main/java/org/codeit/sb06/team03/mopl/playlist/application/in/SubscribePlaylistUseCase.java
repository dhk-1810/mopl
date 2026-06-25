package org.codeit.sb06.team03.mopl.playlist.application.in;

import java.util.UUID;

public interface SubscribePlaylistUseCase {
    void subscribe(UUID playlistId, UUID userId);
}
