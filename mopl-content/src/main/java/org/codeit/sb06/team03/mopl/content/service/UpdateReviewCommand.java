package org.codeit.sb06.team03.mopl.content.service;

import java.util.UUID;

public record UpdateReviewCommand(
        UUID reviewId,
        UUID authorId,
        String text,
        Double rating
) {}
