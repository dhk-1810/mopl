package org.codeit.sb06.team03.mopl.content.controller;

import org.codeit.sb06.team03.mopl.content.domain.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.util.Set;
import java.util.UUID;

public record ContentDto (
        UUID id,
        ContentType type,
        String title,
        String description,
        String thumbnailUrl,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount
) {
    public static ContentDto from(ContentReadModel readModel, String thumbnailUrl) {
        return new ContentDto(
                readModel.id(),
                readModel.type(),
                readModel.title(),
                readModel.description(),
                thumbnailUrl,
                readModel.tags(),
                readModel.averageRating(),
                readModel.reviewCount(),
                readModel.watcherCount()
        );
    }
}
