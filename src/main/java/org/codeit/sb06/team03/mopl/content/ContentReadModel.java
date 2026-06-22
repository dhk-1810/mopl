package org.codeit.sb06.team03.mopl.content;

import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ContentReadModel (
        UUID id,
        ContentType type,
        String title,
        String description,
        String thumbnailUrl,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount,
        Instant createdAt
) {
    public static ContentReadModel from(Content content){
        Set<String> tags = content.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        return new ContentReadModel(
                content.getId(),
                content.getType(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailUrl(),
                tags,
                content.getAverageRating(),
                content.getReviewCount(),
                content.getWatcherCount(),
                content.getCreatedAt()
        );
    }
}
