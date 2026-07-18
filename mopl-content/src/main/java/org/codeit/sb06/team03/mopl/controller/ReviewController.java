package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.dto.response.ReviewDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseReviewDto;
import org.codeit.sb06.team03.mopl.dto.request.ReviewUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ReviewCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestReviewDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.service.composite.ReviewCompositeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewCompositeService reviewCompositeService;

    @PostMapping
    public ResponseEntity<ReviewDto> create(
            @RequestBody @Valid ReviewCreateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        ReviewDto reviewDto = reviewCompositeService.createReview(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewDto);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @PathVariable UUID reviewId,
            @RequestBody @Valid ReviewUpdateRequest request,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        ReviewDto reviewDto = reviewCompositeService.updateReview(reviewId, request, user.getId());
        return ResponseEntity.ok(reviewDto);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal MoplUserDetails user
    ) {
        reviewCompositeService.deleteReview(reviewId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<CursorResponseReviewDto> getAll(
            @ModelAttribute CursorRequestReviewDto request
    ) {
        CursorResponseReviewDto response = reviewCompositeService.getReviews(request);
        return ResponseEntity.ok(response);
    }
}
