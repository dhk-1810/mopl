package org.codeit.sb06.team03.mopl.dto;

import org.codeit.sb06.team03.mopl.enums.ContentType;

public record CollectedContentDto(
        ContentType type,
        String title,
        String description,
        String thumbnailKey
) {}
