package org.codeit.sb06.team03.mopl.playlist.application.in;

import java.util.UUID;

public interface AddCurationUseCase {
    void addContentToPlaylist(UUID playlistId, UUID contentId, String contentTitle, UUID ownerId);
}
