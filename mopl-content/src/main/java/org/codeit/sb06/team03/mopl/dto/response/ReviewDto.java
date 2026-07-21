package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.dto.UserSummary;

import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID contentId,
        UserSummary author,
        String text,
        double rating
) {

}
