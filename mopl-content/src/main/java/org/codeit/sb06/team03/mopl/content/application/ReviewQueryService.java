package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.application.in.GetReviewUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.LoadReviewPort;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewQueryService implements GetReviewUseCase {

    private final LoadReviewPort loadReviewPort;
    private final LoadContentPort loadContentPort;

    @Override
    public Slice<Review> getReviews(
            UUID contentId,
            String cursor,
            UUID idAfter,
            int limit,
            SortReviewBy sortBy,
            SortDirection sortDirection
    ) {
        if (!loadContentPort.existsById(contentId)) {
            throw ContentNotFoundException.fromId(contentId);
        }
        return loadReviewPort.findByContentId(contentId, cursor, idAfter, limit, sortBy, sortDirection);
    }

    @Override
    public long countReviews(UUID contentId) {
        return loadReviewPort.countByContentId(contentId);
    }
}
