package org.codeit.sb06.team03.mopl.content.infra.in;

import java.util.UUID;

public record CursorResponseContentDto(
        Object data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        String sortBy,
        String sortDirection
) {
}
