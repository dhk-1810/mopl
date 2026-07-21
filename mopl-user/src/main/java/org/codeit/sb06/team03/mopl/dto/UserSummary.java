package org.codeit.sb06.team03.mopl.dto;

import java.util.UUID;

public record UserSummary(
        UUID userId,
        String name,
        String profileImageUrl
) {
}
