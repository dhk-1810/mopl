package org.codeit.sb06.team03.mopl.content;

import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.vo.Type;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record ContentReadModel (
        UUID id,
        Type type,
        String title,
        String description,
        String thumbnailUrl,
        Set<String> tags,
        double averageRating,
        int reviewCount,
        long watcherCount
) {
    public static ContentReadModel from(Content content){
        Set<String> tags = content.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        return new ContentReadModel(
                content.getId(),
                content.getType(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailImage(),
                tags,
                content.getAverageRating(),
                content.getReviewCount(),
                content.getWatcherCount()
        );
    }
}
