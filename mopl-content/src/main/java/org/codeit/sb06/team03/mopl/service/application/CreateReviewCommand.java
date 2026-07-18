package org.codeit.sb06.team03.mopl.service.application;

import java.util.UUID;

public record CreateReviewCommand(
        UUID contentId,
        UUID authorId,
        String text,
        double rating
) {}
