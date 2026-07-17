package org.codeit.sb06.team03.mopl.content.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.service.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.content.repository.ReviewRepository;
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
