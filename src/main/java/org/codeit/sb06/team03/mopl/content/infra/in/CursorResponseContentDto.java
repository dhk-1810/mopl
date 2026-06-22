package org.codeit.sb06.team03.mopl.content.infra.in;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortContentBy;

import java.util.UUID;

public record CursorResponseContentDto(
        Object data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        SortDirection sortDirection,
        SortContentBy sortBy
) {
}
