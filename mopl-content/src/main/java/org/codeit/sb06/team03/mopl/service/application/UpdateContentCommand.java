package org.codeit.sb06.team03.mopl.service.application;

import java.util.Set;

public record UpdateContentCommand(
        String title,
        String description,
        Set<String> tags
) {
}
