package org.codeit.sb06.team03.mopl.domain;

import org.codeit.sb06.team03.mopl.domain.entity.Content;
import org.codeit.sb06.team03.mopl.enums.ContentType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ContentReadModel (
        UUID id,
        ContentType type,
        String title,
        String description,
        String thumbnailKey,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount,
        Instant createdAt
) {
    public static ContentReadModel from(Content content, Set<String> tags){
        return new ContentReadModel(
                content.getId(),
                content.getType(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailKey() != null ? content.getThumbnailKey().toString() : null,
                tags,
                content.getAverageRating(),
                content.getReviewCount(),
                content.getWatcherCount(),
                content.getCreatedAt()
        );
    }
}
