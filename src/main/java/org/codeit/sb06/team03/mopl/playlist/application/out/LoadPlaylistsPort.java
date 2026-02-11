package org.codeit.sb06.team03.mopl.playlist.application.out;

import org.codeit.sb06.team03.mopl.playlist.infra.in.PlaylistData;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface LoadPlaylistsPort {

    Slice<PlaylistData> findAll(
            String keywordLike,
            UUID ownerIdEqual,
            UUID subscriberIdEqual,
            String cursor,
            UUID idAfter,
            int limit,
            String sortDirection,
            String sortBy
    );
}
