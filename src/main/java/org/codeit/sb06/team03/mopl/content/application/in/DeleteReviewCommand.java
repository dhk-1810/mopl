package org.codeit.sb06.team03.mopl.content.application.in;

import java.util.UUID;

public record DeleteReviewCommand(
        UUID reviewId,
        UUID authorId
) {}
