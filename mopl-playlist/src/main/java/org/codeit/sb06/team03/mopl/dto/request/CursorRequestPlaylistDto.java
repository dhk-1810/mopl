package org.codeit.sb06.team03.mopl.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record CursorRequestPlaylistDto (

        @Schema(description = "검색 키워드")
        String keywordLike,

        @Schema(description = "소유자 ID")
        UUID ownerIdEqual,

        @Schema(description = "구독자 ID")
        UUID subscriberIdEqual,

        @Schema(description = "커서")
        String cursor,

        @Schema(description = "보조 커서")
        UUID idAfter,

        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "한 번에 가져올 개수"
        )
        @NotNull
        Integer limit,

        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "정렬 방향",
                allowableValues = {"ASCENDING", "DESCENDING"}
        )
        @NotNull
        @Pattern(regexp = "^(ASCENDING|DESCENDING)$")
        String sortDirection,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "정렬 기준",
                allowableValues = {"updatedAt", "subscribeCount"})
        @NotNull
        @Pattern(regexp = "^(updatedAt|subscribeCount)$")
        String sortBy
) {
}
