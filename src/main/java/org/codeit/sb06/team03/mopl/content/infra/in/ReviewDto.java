package org.codeit.sb06.team03.mopl.content.infra.in;

import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;

import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID contentId,
        UserSummary author,
        String text,
        double rating
) {

}
