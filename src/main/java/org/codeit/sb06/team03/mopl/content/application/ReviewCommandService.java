package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.LoadReviewPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveReviewPort;
import org.codeit.sb06.team03.mopl.content.domain.entity.Content;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.domain.exception.ReviewAlreadyExistsException;
import org.codeit.sb06.team03.mopl.content.domain.exception.ReviewNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewCommandService implements CreateReviewUseCase, UpdateReviewUseCase, DeleteReviewUseCase {

    private final LoadReviewPort loadReviewPort;
    private final SaveReviewPort saveReviewPort;
    private final LoadContentPort loadContentPort;
    private final SaveContentPort saveContentPort;

    @Override
    @Transactional
    public Review create(CreateReviewCommand command) {
        if (loadReviewPort.existsByContentIdAndAuthorId(command.contentId(), command.authorId())) {
            throw ReviewAlreadyExistsException.fromContentIdAndAuthorId(command.contentId(), command.authorId());
        }

        Content content = loadContentPort.findById(command.contentId())
                .orElseThrow(() -> ContentNotFoundException.fromId(command.contentId()));

        int ratingInt = (int) command.rating();
        content.addReview(ratingInt);
        saveContentPort.save(content);

        Review review = Review.create(content, command.authorId(), command.text(), ratingInt);
        return saveReviewPort.save(review);
    }

    @Override
    @Transactional
    public Review update(UpdateReviewCommand command) {
        Review review = loadReviewPort.findById(command.reviewId())
                .orElseThrow(() -> ReviewNotFoundException.fromId(command.reviewId()));

        if (!review.getAuthorId().equals(command.authorId())) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        if (command.rating() != null) {
            int newRating = (int) (double) command.rating();
            if (newRating != review.getRating()) {
                Content content = review.getContent();
                content.updateReview(review.getRating(), newRating);
                saveContentPort.save(content);
            }
        }

        review.update(command.text(), command.rating() != null ? (int) (double) command.rating() : null);
        return saveReviewPort.save(review);
    }

    @Override
    @Transactional
    public void delete(DeleteReviewCommand command) {
        Review review = loadReviewPort.findById(command.reviewId())
                .orElseThrow(() -> ReviewNotFoundException.fromId(command.reviewId()));

        if (!review.getAuthorId().equals(command.authorId())) {
            throw new AccessDeniedException("You are not the author of this review");
        }

        Content content = review.getContent();
        content.removeReview(review.getRating());
        saveContentPort.save(content);

        saveReviewPort.deleteById(command.reviewId());
    }
}
