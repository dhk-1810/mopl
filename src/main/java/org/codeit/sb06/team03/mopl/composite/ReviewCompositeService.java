package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorRequestReviewDto;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseReviewDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ReviewCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ReviewDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ReviewUpdateRequest;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewCompositeService {

    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final GetReviewUseCase getReviewUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public ReviewDto createReview(ReviewCreateRequest request, UUID authorId) {
        Review review = createReviewUseCase.create(new CreateReviewCommand(
                request.contentId(),
                authorId,
                request.text(),
                request.rating()
        ));

        return getReviewDto(authorId, review);
    }

    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID authorId) {
        Review review = updateReviewUseCase.update(new UpdateReviewCommand(
                reviewId,
                authorId,
                request.text(),
                request.rating()
        ));

        return getReviewDto(authorId, review);
    }

    public void deleteReview(UUID reviewId, UUID authorId) {
        deleteReviewUseCase.delete(new DeleteReviewCommand(
                reviewId,
                authorId
        ));
    }

    public CursorResponseReviewDto getReviews(CursorRequestReviewDto request) {
        UUID contentId = request.contentId();

        Slice<Review> slice = getReviewUseCase.getReviews(
                contentId,
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortReviewBy(),
                request.sortDirection()
        );

        List<Review> reviews = slice.getContent();
        List<UUID> authorIds = reviews.stream().map(Review::getAuthorId).distinct().toList();
        Map<UUID, ProfileReadModel> profilesMap = getProfileUseCase.getProfileReadModels(authorIds);

        Map<UUID, UserSummary> authorsMap = profilesMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ProfileReadModel profile = entry.getValue();
                            String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
                            return new UserSummary(profile.userId(), profile.name(), url);
                        }
                ));

        List<ReviewDto> data = reviews.stream()
                .map(review -> {
                    UserSummary author = authorsMap.get(review.getAuthorId());
                    if (author == null) {
                        author = new UserSummary(review.getAuthorId(), "Unknown", null);
                    }
                    return new ReviewDto(
                            review.getId(),
                            review.getContent().getId(),
                            author,
                            review.getText(),
                            review.getRating()
                    );
                })
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

        long totalCount = getReviewUseCase.countReviews(contentId);

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
        ProfileReadModel profile = getProfileUseCase.getProfileReadModel(authorId);
        String profileUrl = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
        UserSummary author = new UserSummary(profile.userId(), profile.name(), profileUrl);

        return new ReviewDto(
                review.getId(),
                review.getContent().getId(),
                author,
                review.getText(),
                review.getRating()
        );
    }
}
