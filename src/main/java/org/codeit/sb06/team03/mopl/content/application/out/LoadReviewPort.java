package org.codeit.sb06.team03.mopl.content.application.out;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.springframework.data.domain.Slice;

import java.util.Optional;
import java.util.UUID;

public interface LoadReviewPort {

    Optional<Review> findById(UUID reviewId);

    Slice<Review> findByContentId(
            UUID contentId,
            String cursor,
            UUID idAfter,
            int limit,
            SortReviewBy sortBy,
            SortDirection sortDirection
    );

    long countByContentId(UUID contentId);
}
