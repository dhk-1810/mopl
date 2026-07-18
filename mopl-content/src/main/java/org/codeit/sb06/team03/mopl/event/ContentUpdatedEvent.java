package org.codeit.sb06.team03.mopl.event;

import org.codeit.sb06.team03.mopl.enums.ContentType;

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
