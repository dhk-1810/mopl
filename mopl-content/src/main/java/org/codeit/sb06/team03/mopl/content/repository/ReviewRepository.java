package org.codeit.sb06.team03.mopl.content.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.service.SortReviewBy;
import org.codeit.sb06.team03.mopl.content.domain.entity.Review;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.codeit.sb06.team03.mopl.content.domain.entity.QReview.review;

public interface ReviewRepository extends QuerydslJpaRepository<Review, UUID> {

    long countByContentId(UUID contentId);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    default Slice<Review> findByContentId(
            UUID contentId,
            String cursor,
            UUID idAfter,
            int limit,
            SortReviewBy sortBy,
            SortDirection sortDirection
    ) {
        List<Review> reviews = select(review)
                .from(review)
                .where(
                        review.content.id.eq(contentId),
                        cursorPredicate(cursor, idAfter, sortBy, sortDirection)
                )
                .orderBy(orderByExpressions(sortBy, sortDirection))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = false;
        if (reviews.size() > limit) {
            reviews.remove(limit);
            hasNext = true;
        }

        return new SliceImpl<>(reviews, PageRequest.ofSize(limit), hasNext);
    }

    private static BooleanExpression cursorPredicate(
            String cursor,
            UUID idAfter,
            SortReviewBy sortBy,
            SortDirection sortDirection
    ) {
        if (cursor == null || idAfter == null) {
            return null;
        }

        boolean isDesc = sortDirection == SortDirection.DESCENDING;
        if (sortBy == SortReviewBy.createdAt) {
            Instant cursorInstant = Instant.parse(cursor);
            BooleanExpression valueCompare = isDesc ? review.createdAt.lt(cursorInstant) : review.createdAt.gt(cursorInstant);
            BooleanExpression idCompare = isDesc ? review.id.lt(idAfter) : review.id.gt(idAfter);
            return valueCompare.or(review.createdAt.eq(cursorInstant).and(idCompare));
        } else { // sortBy == SortReviewBy.rating
            double cursorRating = Double.parseDouble(cursor);
            BooleanExpression valueCompare = isDesc ? review.rating.lt(cursorRating) : review.rating.gt(cursorRating);
            BooleanExpression idCompare = isDesc ? review.id.lt(idAfter) : review.id.gt(idAfter);
            return valueCompare.or(review.rating.eq((int) cursorRating).and(idCompare));
        }
    }

    private static OrderSpecifier<?>[] orderByExpressions(SortReviewBy sortBy, SortDirection sortDirection) {
        boolean isDesc = sortDirection == SortDirection.DESCENDING;
        Order order = isDesc ? Order.DESC : Order.ASC;

        OrderSpecifier<?> primaryOrder;
        if (sortBy == SortReviewBy.createdAt) {
            primaryOrder = new OrderSpecifier<>(order, review.createdAt);
        } else {
            primaryOrder = new OrderSpecifier<>(order, review.rating);
        }

        OrderSpecifier<UUID> secondaryOrder = new OrderSpecifier<>(order, review.id);

        return new OrderSpecifier<?>[]{primaryOrder, secondaryOrder};
    }
}
