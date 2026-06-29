package org.codeit.sb06.team03.mopl.content.application.out;

import org.codeit.sb06.team03.mopl.content.domain.entity.Review;

import java.util.UUID;

public interface SaveReviewPort {

    Review save(Review review);

    void deleteById(UUID reviewId);
}
