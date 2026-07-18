package org.codeit.sb06.team03.mopl.dto.request;

import org.codeit.sb06.team03.mopl.enums.ContentType;

import java.util.Set;

public record ContentCreateRequest (
        ContentType type,
        String title,
        String description,
        Set<String> tags
) {
}
