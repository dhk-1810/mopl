package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.common.WatchingSessionResponse;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;

import java.util.List;
import java.util.UUID;

public record CursorResponseWatchingSessionDto(

        List<WatchingSessionResponse> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        SortDirection sortDirection,
        String sortBy
) {

}
