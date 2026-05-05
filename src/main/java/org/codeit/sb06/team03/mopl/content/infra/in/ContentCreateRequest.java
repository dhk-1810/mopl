package org.codeit.sb06.team03.mopl.content.infra.in;

import org.codeit.sb06.team03.mopl.content.domain.vo.Type;

import java.util.Set;

public record ContentCreateRequest (
        Type type,
        String title,
        String description,
        Set<String> tags
) {
}
