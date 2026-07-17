package org.codeit.sb06.team03.mopl.content.controller;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.service.SortContentBy;
import org.codeit.sb06.team03.mopl.content.controller.ContentDto;

import java.util.List;
import java.util.UUID;

public record CursorResponseContentDto(
        List<ContentDto> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        SortContentBy sortBy,
        SortDirection sortDirection
) {
}
