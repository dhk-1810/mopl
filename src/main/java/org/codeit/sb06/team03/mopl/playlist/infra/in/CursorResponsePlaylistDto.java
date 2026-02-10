package org.codeit.sb06.team03.mopl.playlist.infra.in;

import java.util.List;

public record CursorResponsePlaylistDto (

        List<PlaylistDto> data,
        String nextCursor,
        String nextIdAfter,
        Boolean hasNext,
        Long totalCount,
        String sortBy,
        SortOrder sortDirection
) {
    public enum SortOrder {
        ASCENDING, DESCENDING;
    }
}
