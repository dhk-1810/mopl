package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record CursorResponseWatchingSessionDto(
        List<WatchingSessionDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        SortDirection sortDirection,
        String sortBy
) {

}
