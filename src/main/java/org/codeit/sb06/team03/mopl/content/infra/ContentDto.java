package org.codeit.sb06.team03.mopl.content.infra;

import java.util.List;
import java.util.UUID;

public record ContentDto (
        UUID id,
        String type,
        String title,
        String description,
        String thumbnailUrl,
        List<String> tags, // TODO 확인필요
        double averageRating,
        int reviewCount,
        long watcherCount
){
}
