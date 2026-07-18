package org.codeit.sb06.team03.mopl.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotBlank
        @Size(max = 1000)
        String content
) {
}
