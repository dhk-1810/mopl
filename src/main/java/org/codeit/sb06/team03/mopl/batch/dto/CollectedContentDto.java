package org.codeit.sb06.team03.mopl.batch.dto;

import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

public record CollectedContentDto(
        ContentType type,
        String title,
        String description,
        String thumbnailKey
) {}
