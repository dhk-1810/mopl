package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.enums.SortReviewBy;
import org.codeit.sb06.team03.mopl.domain.entity.Review;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.ReviewRepository;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager", readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;

    public Slice<Review> getReviews(
            UUID contentId,
            String cursor,
            UUID idAfter,
            int limit,
            SortReviewBy sortBy,
            SortDirection sortDirection
    ) {
        if (!contentRepository.existsById(contentId)) {
            throw ContentNotFoundException.fromId(contentId);
        }
        return reviewRepository.findByContentId(contentId, cursor, idAfter, limit, sortBy, sortDirection);
    }

    public long countReviews(UUID contentId) {
        return reviewRepository.countByContentId(contentId);
    }
}
