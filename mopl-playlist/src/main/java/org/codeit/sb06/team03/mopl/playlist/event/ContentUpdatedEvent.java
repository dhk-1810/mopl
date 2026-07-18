package org.codeit.sb06.team03.mopl.playlist.event;

import java.util.Set;
import java.util.UUID;

public record ContentUpdatedEvent(
        UUID contentId,
        String type,
        String title,
        String description,
        String thumbnailKey,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount
) {
}
