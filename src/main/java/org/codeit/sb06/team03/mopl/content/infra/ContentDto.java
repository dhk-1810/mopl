package org.codeit.sb06.team03.mopl.content.infra;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.domain.vo.Type;

import java.util.Set;
import java.util.UUID;

public record ContentDto (
        UUID id,
        Type type,
        String title,
        String description,
        String thumbnailUrl,
        Set<String> tags, // TODO 확인필요
        double averageRating,
        int reviewCount,
        long watcherCount
) {
    public static ContentDto from(ContentReadModel readModel, long watcherCount) {
        return new ContentDto(
                readModel.id(),
                readModel.type(),
                readModel.title(),
                readModel.description(),
                readModel.thumbnailUrl(),
                readModel.tags(),
                readModel.averageRating(),
                readModel.reviewCount(),
                watcherCount
        );
    }
}
