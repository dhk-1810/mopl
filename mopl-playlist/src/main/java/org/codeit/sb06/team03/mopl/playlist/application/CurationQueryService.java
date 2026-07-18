package org.codeit.sb06.team03.mopl.playlist.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.infra.out.CurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class CurationQueryService {

    private final CurationRepository curationRepository;

    public Map<UUID, List<UUID>> getContentIdsByPlaylistIds(Set<UUID> playlistIds) {
        return curationRepository.findAllByPlaylistIdsIn(playlistIds);
    }
}
