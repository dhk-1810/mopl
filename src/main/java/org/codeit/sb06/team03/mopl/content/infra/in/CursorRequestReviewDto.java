package org.codeit.sb06.team03.mopl.content.infra.in;

import jakarta.validation.constraints.NotNull;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record CursorRequestReviewDto(

        @Nullable
        UUID contentId,

        @Nullable
        String cursor,

        @Nullable
        UUID idAfter,

        @NotNull
        int limit,

        @NotNull
        SortReviewBy sortReviewBy,

        @NotNull
        SortDirection sortDirection

) {

}
