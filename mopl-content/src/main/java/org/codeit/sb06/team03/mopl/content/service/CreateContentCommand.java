package org.codeit.sb06.team03.mopl.content.service;

import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import java.util.Set;

public record CreateContentCommand(
        ContentType type,
        String title,
        String description,
        Set<String> tags
) {
}
