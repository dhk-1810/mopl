package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.application.out.LoadReviewPort;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadReviewAdapter implements LoadReviewPort {

    private final ReviewRepository repository;

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return repository.findById(reviewId);
    }

    @Override
    public Slice<Review> findByContentId(
            UUID contentId,
            String cursor,
            UUID idAfter,
            int limit,
            SortReviewBy sortBy,
            SortDirection sortDirection
    ) {
        return repository.findByContentId(contentId, cursor, idAfter, limit, sortBy, sortDirection);
    }

    @Override
    public long countByContentId(UUID contentId) {
        return repository.countByContentId(contentId);
    }

    @Override
    public boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId) {
        return repository.existsByContentIdAndAuthorId(contentId, authorId);
    }
}

