package org.codeit.sb06.team03.mopl.content.infra;

import java.util.List;
import java.util.UUID;

public record CursorRequestContentDto (
        String typeEqual,
        String keywordLike,
        List<Object> tagsIn, // TODO 확인 필요
        String cursor,
        UUID idAfter,
        int limit,
        String sortDirection,
        String sortBy
) {
}
