package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.domain.entity.Content;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.infra.in.*;
import org.codeit.sb06.team03.mopl.content.infra.out.ContentRepository;
import org.codeit.sb06.team03.mopl.content.infra.out.ReviewRepository;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummary;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;
    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    @Transactional
    public ReviewDto createReview(ReviewCreateRequest request, UUID authorId) {
        Content content = contentRepository.findById(request.contentId())
                .orElseThrow(() -> new NoSuchElementException("Content not found: " + request.contentId()));

        int ratingInt = (int) request.rating();
        content.addReview(ratingInt);
        contentRepository.save(content);

        Review review = Review.create(content, authorId, request.text(), ratingInt);
        reviewRepository.save(review);

        ProfileReadModel profile = getProfileUseCase.getProfileReadModel(authorId);
        String profileUrl = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
        UserSummary author = new UserSummary(profile.userId(), profile.name(), profileUrl);

        return new ReviewDto(review.getId(), content.getId(), author, review.getText(), review.getRating());
    }

    @Transactional
    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID authorId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

        if (!review.getAuthorId().equals(authorId)) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        if (request.rating() != null) {
            int newRating = (int) (double) request.rating();
            if (newRating != review.getRating()) {
                Content content = review.getContent();
                content.updateReview(review.getRating(), newRating);
                contentRepository.save(content);
            }
        }

        review.update(request.text(), request.rating() != null ? (int) (double) request.rating() : null);
        reviewRepository.save(review);

        ProfileReadModel profile = getProfileUseCase.getProfileReadModel(authorId);
        String profileUrl = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
        UserSummary author = new UserSummary(profile.userId(), profile.name(), profileUrl);

        return new ReviewDto(review.getId(), review.getContent().getId(), author, review.getText(), review.getRating());
    }

    @Transactional
    public void deleteReview(UUID reviewId, UUID authorId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

        if (!review.getAuthorId().equals(authorId)) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        Content content = review.getContent();
        content.removeReview(review.getRating());
        contentRepository.save(content);

        reviewRepository.deleteById(reviewId);
    }

    public CursorResponseReviewDto getReviews(CursorRequestReviewDto request) {
        UUID contentId = request.contentId();
        if (contentId == null) {
            return new CursorResponseReviewDto(List.of(), null, null, false, 0, request.sortReviewBy(), request.sortDirection());
        }

        Slice<Review> slice = reviewRepository.findByContentId(
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
            Review lastReview = reviews.get(reviews.size() - 1);
            nextIdAfter = lastReview.getId();
            if (request.sortReviewBy() == SortReviewBy.createdAt) {
                nextCursor = lastReview.getCreatedAt().toString();
            } else if (request.sortReviewBy() == SortReviewBy.rating) {
                nextCursor = String.valueOf((double) lastReview.getRating());
            }
        }

        long totalCount = reviewRepository.countByContentId(contentId);

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
}
