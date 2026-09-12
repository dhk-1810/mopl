package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.codeit.sb06.team03.mopl.enums.SortReviewBy;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.entity.Review;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestReviewDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseReviewDto;
import org.codeit.sb06.team03.mopl.dto.request.ReviewCreateRequest;
import org.codeit.sb06.team03.mopl.dto.response.ReviewDto;
import org.codeit.sb06.team03.mopl.dto.request.ReviewUpdateRequest;
import org.codeit.sb06.team03.mopl.service.application.ReviewCommandService;
import org.codeit.sb06.team03.mopl.service.application.ReviewQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReviewCompositeService {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ProfileQueryService profileQueryService;
    private final ImageQueryService imageQueryService;

    public ReviewDto createReview(ReviewCreateRequest request, UUID authorId) {
        Review review = reviewCommandService.create(request, authorId);

        return getReviewDto(authorId, review);
    }

    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID authorId) {
        Review review = reviewCommandService.update(reviewId, request, authorId);

        return getReviewDto(authorId, review);
    }

    public void deleteReview(UUID reviewId, UUID authorId) {
        reviewCommandService.delete(reviewId, authorId);
    }

    public CursorResponseReviewDto getReviews(CursorRequestReviewDto request) {
        UUID contentId = request.contentId();

        Slice<Review> slice = reviewQueryService.getReviews(
                contentId,
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortReviewBy(),
                request.sortDirection()
        );

        List<Review> reviews = slice.getContent();
        List<ReviewDto> data = reviews.stream()
                .map(review -> getReviewDto(review.getAuthorId(), review))
                .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext() && !reviews.isEmpty()) {
            Review lastReview = reviews.getLast();
            nextIdAfter = lastReview.getId();
            if (request.sortReviewBy() == SortReviewBy.createdAt) {
                nextCursor = lastReview.getCreatedAt().toString();
            } else if (request.sortReviewBy() == SortReviewBy.rating) {
                nextCursor = String.valueOf((double) lastReview.getRating());
            }
        }

        long totalCount = reviewQueryService.countReviews(contentId);

        return new CursorResponseReviewDto(
                data,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                totalCount,
                request.sortReviewBy(),
                request.sortDirection()
        );
    }

    private ReviewDto getReviewDto(UUID authorId, Review review) {
        String name = "Unknown User";
        String imageKey = null;
        try {
            Profile profile = profileQueryService.getById(authorId);
            if (profile != null) {
                name = profile.getName();
                imageKey = profile.getImageKey();
            }
        } catch (Exception ignored) {
        }
        String profileUrl = imageQueryService.getPresignedUrl(imageKey);
        UserSummary author = new UserSummary(authorId, name, profileUrl);

        return new ReviewDto(
                review.getId(),
                review.getContent().getId(),
                author,
                review.getText(),
                review.getRating()
        );
    }
}
