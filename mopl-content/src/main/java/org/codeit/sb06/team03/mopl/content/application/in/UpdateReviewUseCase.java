package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.domain.entity.Review;

public interface UpdateReviewUseCase {
    Review update(UpdateReviewCommand command);
}
