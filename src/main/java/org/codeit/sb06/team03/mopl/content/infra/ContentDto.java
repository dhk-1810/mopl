package org.codeit.sb06.team03.mopl.content.infra;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;

import java.util.Set;
import java.util.UUID;

public record ContentDto (
        UUID id,
        ContentType type,
        String title,
        String description,
        String thumbnailPresignedUrl,
        Set<String> tags,
        double averageRating,
        long reviewCount,
        long watcherCount
) {
    public static ContentDto from(ContentReadModel readModel, String presignedUrl) {
        return new ContentDto(
                readModel.id(),
                readModel.type(),
                readModel.title(),
                readModel.description(),
                presignedUrl, // TODO
                readModel.tags(),
                readModel.averageRating(),
                readModel.reviewCount(),
                readModel.watcherCount()
        );
    }
}
