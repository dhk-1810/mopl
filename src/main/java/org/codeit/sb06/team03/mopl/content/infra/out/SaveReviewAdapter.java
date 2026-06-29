package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.out.SaveReviewPort;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class SaveReviewAdapter implements SaveReviewPort {

    private final ReviewRepository repository;

    @Override
    public Review save(Review review) {
        return repository.save(review);
    }

    @Override
    public void deleteById(UUID reviewId) {
        repository.deleteById(reviewId);
    }
}
