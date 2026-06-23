package org.codeit.sb06.team03.mopl.content;

import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ContentReadModel (
        UUID id,
        ContentType type,
        String title,
        String description,
        UUID thumbnailKey,
        Set<String> tags, // TODO
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
                content.getThumbnailKey(),
                tags,
                content.getAverageRating(),
                content.getReviewCount(),
                content.getWatcherCount(),
                content.getCreatedAt()
        );
    }
}
