package org.codeit.sb06.team03.mopl.content.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.content.application.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            @RequestBody @Valid ReviewCreateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        ReviewDto reviewDto = reviewService.createReview(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewUpdateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        ReviewDto reviewDto = reviewService.updateReview(reviewId, request, user.getId());
        return ResponseEntity.ok(reviewDto);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        reviewService.deleteReview(reviewId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CursorResponseReviewDto> getReviews(
            @ModelAttribute CursorRequestReviewDto request
    ) {
        CursorResponseReviewDto response = reviewService.getReviews(request);
        return ResponseEntity.ok(response);
    }
}
