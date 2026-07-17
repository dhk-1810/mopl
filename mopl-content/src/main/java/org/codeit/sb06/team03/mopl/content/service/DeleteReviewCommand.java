package org.codeit.sb06.team03.mopl.content.service;

import java.util.UUID;

public record DeleteReviewCommand(
        UUID reviewId,
        UUID authorId
) {}
