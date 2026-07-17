package org.codeit.sb06.team03.mopl.content.domain.event;

import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.util.Set;
import java.util.UUID;

public record ContentUpdatedEvent(
        UUID contentId,
        ContentType type,
        String title,
        String description,
        String thumbnailKey,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount
) {
}
