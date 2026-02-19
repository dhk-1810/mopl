package org.codeit.sb06.team03.mopl.notification.infra.in;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CursorRequestNotificationDto (

        String cursor,

        String idAfter,

        @NotNull
        @Min(1)
        Integer limit,

        @NotBlank
        String sortDirection,

        @NotBlank
        String sortBy
) {
}
