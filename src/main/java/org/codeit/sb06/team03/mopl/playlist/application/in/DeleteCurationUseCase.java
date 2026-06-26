package org.codeit.sb06.team03.mopl.playlist.application.in;

import java.util.UUID;

public interface DeleteCurationUseCase {

    void deleteContentFromPlaylist(UUID playlistId, UUID contentId, UUID ownerId);

    void deleteCurationByContentId(UUID contentId);
}
