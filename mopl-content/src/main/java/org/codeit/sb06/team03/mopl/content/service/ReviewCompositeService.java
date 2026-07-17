package org.codeit.sb06.team03.mopl.content.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.content.service.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.service.*;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.content.controller.CursorRequestReviewDto;
import org.codeit.sb06.team03.mopl.content.controller.CursorResponseReviewDto;
import org.codeit.sb06.team03.mopl.content.controller.ReviewCreateRequest;
import org.codeit.sb06.team03.mopl.content.controller.ReviewDto;
import org.codeit.sb06.team03.mopl.content.controller.ReviewUpdateRequest;
import org.codeit.sb06.team03.mopl.image.service.ImageQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ReviewCompositeService {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ImageQueryService imageQueryService;

    public ReviewDto createReview(ReviewCreateRequest request, UUID authorId) {
        Review review = reviewCommandService.create(new CreateReviewCommand(
                request.contentId(),
                authorId,
                request.text(),
                request.rating()
        ));

        return getReviewDto(authorId, review);
    }

    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID authorId) {
        Review review = reviewCommandService.update(new UpdateReviewCommand(
                reviewId,
                authorId,
                request.text(),
                request.rating()
        ));

        return getReviewDto(authorId, review);
    }

    public void deleteReview(UUID reviewId, UUID authorId) {
        reviewCommandService.delete(new DeleteReviewCommand(
                reviewId,
                authorId
        ));
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
        ExternalUserView profile = externalUserQueryService.getProfile(authorId);
        String name = "Unknown User";
        String imageKey = null;
        if (profile != null) {
            name = profile.getName();
            imageKey = profile.getProfileImageKey();
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
