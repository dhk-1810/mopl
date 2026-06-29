package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.domain.entity.Review;

public interface CreateReviewUseCase {
    Review create(CreateReviewCommand command);
}
