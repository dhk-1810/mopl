package org.codeit.sb06.team03.mopl.dto.request;

import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.enums.SortContentBy;

import java.util.Set;
import java.util.UUID;

public record CursorRequestContentDto (
        String typeEqual,
        String keywordLike,
        Set<String> tagsIn, // 미사용
        String cursor,
        UUID idAfter,
        int limit,
        SortContentBy sortBy,
        SortDirection sortDirection
) {
}
