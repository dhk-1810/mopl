package org.codeit.sb06.team03.mopl.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PlaylistCreateRequest(

        @NotBlank
        String title,

        @NotBlank
        String description

) {
}
