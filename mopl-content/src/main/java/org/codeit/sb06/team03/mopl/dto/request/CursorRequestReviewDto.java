package org.codeit.sb06.team03.mopl.dto.request;

import jakarta.validation.constraints.NotNull;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.enums.SortReviewBy;
import org.springframework.lang.Nullable;

import java.util.UUID;

public record CursorRequestReviewDto(

        @NotNull
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
