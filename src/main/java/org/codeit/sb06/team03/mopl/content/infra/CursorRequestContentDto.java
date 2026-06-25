package org.codeit.sb06.team03.mopl.content.infra;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortContentBy;

import java.util.List;
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
