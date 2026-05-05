package org.codeit.sb06.team03.mopl.content.infra.in;

import java.util.Set;

public record ContentUpdateRequest(
        String title,
        String description,
        Set<String> tags
) {
}
