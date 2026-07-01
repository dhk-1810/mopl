package org.codeit.sb06.team03.mopl.playlist.application.in;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface GetCurationUseCase {
    Map<UUID, List<UUID>> getContentIdsByPlaylistIds(Set<UUID> playlistIds);
}
