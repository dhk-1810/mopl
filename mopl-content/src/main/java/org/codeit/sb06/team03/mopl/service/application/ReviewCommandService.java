package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.Review;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.exception.ReviewAlreadyExistsException;
import org.codeit.sb06.team03.mopl.exception.ReviewNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager", readOnly = true)
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;

    @Transactional("contentTransactionManager")
    public Review create(CreateReviewCommand command) {
        if (reviewRepository.existsByContentIdAndAuthorId(command.contentId(), command.authorId())) {
            throw ReviewAlreadyExistsException.fromContentIdAndAuthorId(command.contentId(), command.authorId());
        }

        Content content = contentRepository.findById(command.contentId())
                .orElseThrow(() -> ContentNotFoundException.fromId(command.contentId()));

        int ratingInt = (int) command.rating();
        content.addReview(ratingInt);
        contentRepository.save(content);

        Review review = Review.create(content, command.authorId(), command.text(), ratingInt);
        return reviewRepository.save(review);
    }

    @Transactional("contentTransactionManager")
    public Review update(UpdateReviewCommand command) {
        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> ReviewNotFoundException.fromId(command.reviewId()));

        if (!review.getAuthorId().equals(command.authorId())) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        if (command.rating() != null) {
            int newRating = (int) (double) command.rating();
            if (newRating != review.getRating()) {
                Content content = review.getContent();
                content.updateReview(review.getRating(), newRating);
                contentRepository.save(content);
            }
        }

        review.update(command.text(), command.rating() != null ? (int) (double) command.rating() : null);
        return reviewRepository.save(review);
    }

    @Transactional("contentTransactionManager")
    public void delete(DeleteReviewCommand command) {
        Review review = reviewRepository.findById(command.reviewId())
                .orElseThrow(() -> ReviewNotFoundException.fromId(command.reviewId()));

        if (!review.getAuthorId().equals(command.authorId())) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        Content content = review.getContent();
        content.removeReview(review.getRating());
        contentRepository.save(content);

        reviewRepository.deleteById(command.reviewId());
    }
}
