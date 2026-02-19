package org.codeit.sb06.team03.mopl.notification.infra.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CursorRequestNotificationDto (

        String cursor,

        String idAfter,

        @NotNull
        Integer limit,

        @NotBlank
        String sortDirection,

        @NotBlank
        String sortBy
) {
}
