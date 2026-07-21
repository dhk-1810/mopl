package org.codeit.sb06.team03.mopl.dto;

import java.util.List;

public record CursorResponseContentDto(
        List<ContentDto> data,
        boolean hasNext
) {
}
