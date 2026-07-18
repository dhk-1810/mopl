package org.codeit.sb06.team03.mopl.service.application;

import org.codeit.sb06.team03.mopl.enums.ContentType;
import java.util.Set;

public record CreateContentCommand(
        ContentType type,
        String title,
        String description,
        Set<String> tags
) {
}
