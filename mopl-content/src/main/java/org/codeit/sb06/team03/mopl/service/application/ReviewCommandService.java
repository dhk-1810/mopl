package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.ReviewCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ReviewUpdateRequest;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.Review;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.exception.ReviewAlreadyExistsException;
import org.codeit.sb06.team03.mopl.exception.ReviewNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.ReviewRepository;
import org.codeit.sb06.team03.mopl.entity.ContentTagService;
import org.codeit.sb06.team03.mopl.event.ContentUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager", readOnly = true)
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ContentTagService contentTagService;

    @Transactional("contentTransactionManager")
    public Review create(ReviewCreateRequest request, UUID authorId) {
        if (reviewRepository.existsByContentIdAndAuthorId(request.contentId(), authorId)) {
            throw ReviewAlreadyExistsException.fromContentIdAndAuthorId(request.contentId(), authorId);
        }

        Content content = contentRepository.findById(request.contentId())
                .orElseThrow(() -> ContentNotFoundException.fromId(request.contentId()));

        int ratingInt = (int) request.rating();
        content.addReview(ratingInt);
        contentRepository.save(content);

        Review review = Review.create(content, authorId, request.text(), ratingInt);
        Review saved = reviewRepository.save(review);
        publishContentUpdatedEvent(content);
        return saved;
    }

    @Transactional("contentTransactionManager")
    public Review update(UUID reviewId, ReviewUpdateRequest request, UUID authorId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewNotFoundException.fromId(reviewId));

        if (!review.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("You are not the author of this review");
        }

        boolean ratingChanged = false;
        if (request.rating() != null) {
            int newRating = (int) (double) request.rating();
            if (newRating != review.getRating()) {
                Content content = review.getContent();
                content.updateReview(review.getRating(), newRating);
                contentRepository.save(content);
                ratingChanged = true;
            }
        }

        review.update(request.text(), request.rating() != null ? (int) (double) request.rating() : null);
        Review saved = reviewRepository.save(review);
        if (ratingChanged) {
            publishContentUpdatedEvent(review.getContent());
        }
        return saved;
    }

    @Transactional("contentTransactionManager")
    public void delete(UUID reviewId, UUID authorId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> ReviewNotFoundException.fromId(reviewId));

        if (!review.getAuthorId().equals(authorId)) {
            throw new IllegalArgumentException("You are not the author of this review");
        }

        Content content = review.getContent();
        content.removeReview(review.getRating());
        contentRepository.save(content);

        reviewRepository.deleteById(reviewId);
        publishContentUpdatedEvent(content);
    }

    private void publishContentUpdatedEvent(Content content) {
        Set<String> tags = contentTagService.getByContentId(content.getId());
        eventPublisher.publishEvent(new ContentUpdatedEvent(
                content.getId(),
                content.getType(),
                content.getTitle(),
                content.getDescription(),
                content.getThumbnailKey(),
                tags,
                content.getAverageRating(),
                content.getReviewCount(),
                content.getWatcherCount()
        ));
    }
}
