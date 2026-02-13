package org.codeit.sb06.team03.mopl.playlist.application.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Curation;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.CurationId;

import java.util.*;

public interface LoadCurationPort {

    boolean existsById(CurationId id);

    Optional<Curation> findById(CurationId id);

    List<UUID> findAllByPlaylistId(UUID playlistId);

    Map<UUID, List<UUID>> findAllByPlaylistIdsIn(List<UUID> playlistIds);

}
