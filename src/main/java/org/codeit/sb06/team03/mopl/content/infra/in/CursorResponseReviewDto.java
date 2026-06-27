package org.codeit.sb06.team03.mopl.content.infra.in;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;

import java.util.List;
import java.util.UUID;

public record CursorResponseReviewDto(
        List<ReviewDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        SortReviewBy sortBy,
        SortDirection sortDirection
) {}
