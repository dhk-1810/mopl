package org.codeit.sb06.team03.mopl.content.infra.out;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.SortContentBy;
import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.*;

import static org.codeit.sb06.team03.mopl.account.domain.QAccount.account;
import static org.codeit.sb06.team03.mopl.content.QContent.content;
import static org.codeit.sb06.team03.mopl.user.domain.QProfile.profile;
import static org.codeit.sb06.team03.mopl.watchingSession.domain.QWatchingSession.watchingSession;

public interface ContentRepository extends QuerydslJpaRepository<Content, UUID> {

    @Query("""
            SELECT c
            FROM Content c
            LEFT JOIN FETCH c.tags
            WHERE c.id = :id
            """)
    Optional<Content> findByIdWithTags(UUID id);

    default Slice<ContentReadModel> findAll(
            String typeEqual,
            String keywordLike,
            Set<String> tagsIn,
            String cursor,
            UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    ) {
        Predicate[] predicates = {
                keywordLikePredicate(keywordLike),
                typeEqualPredicate(typeEqual),
                cursorExpressionPredicate(cursor, idAfter, sortDirection, sortBy)
        };

        var contents = select(Projections.constructor(ContentReadModel.class,
                    content.id,
                    content.type,
                    content.description,
                    content.thumbnailImage,
                    content.tags,
                    content.averageRating,
                    content.reviewCount,
                    content.watcherCount
                ))
                .from(content)
                .where(predicates)
                .orderBy(orderByExpressions(sortDirection, sortBy))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = false;
        if (contents.size() > limit) {
            contents.remove(limit);
            hasNext = true;
        }

        return new SliceImpl<>(contents, PageRequest.ofSize(limit), hasNext);
    }

//    default List<WatchingSessionResponse> findSessionsDetails(WatchingSessionCursorQuery query) {
//        OrderSpecifier<Instant> primaryOrder = getPrimaryOrder(query.sortDirection());
//        OrderSpecifier<UUID> secondaryOrder = getSecondaryOrder(query.sortDirection());
//
//        BooleanExpression cursorAndAssistanceCursorCondition =
//                getCursorAndAssistanceCursorCondition(query.cursor(), query.idAfter(), query.sortDirection());
//
//        BooleanExpression watcherNameLikeCondition = getWatcherNameLikeCondition(query.watcherNameLike());
//
//        List<Tuple> tuples = select(
//                watchingSession.id,
//                watchingSession.createdAt,
//                profile.accountId,
//                profile.name,
//                timeoutImage.presignedUrl,
//                content.id,
//                content.type.stringValue(),
//                content.title,
//                content.description,
//                content.thumbnailImage,
//                reviewStats.ratingSum,
//                reviewStats.reviewCount
//        )
//                .from(content)
//                .innerJoin(content.reviewStats, reviewStats)
//                .innerJoin(watchingSession).on(content.id.eq(watchingSession.liveChatId))
//                .innerJoin(profile).on(watchingSession.watcherId.eq(profile.accountId))
//                .leftJoin(timeoutImage).on(profile.timeoutImage.eq(timeoutImage))
//                .where(
//                        content.id.eq(query.contentId()),
//                        cursorAndAssistanceCursorCondition,
//                        watcherNameLikeCondition
//                )
//                .limit(query.limit() + 1)
//                .orderBy(primaryOrder, secondaryOrder)
//                .fetch();
//
//        List<String> tags = !tuples.isEmpty() ? getTags(query.contentId()) : List.of();
//
//
//        return tuples.stream().map(tuple ->
//                combineSessionDetails(tuple, tags)).collect(Collectors.toCollection(ArrayList::new));
//    }

    default long countByContentIdAndWatcherNameLike(UUID contentId, @Nullable String watcherNameLike) {
        BooleanExpression watcherNameLikeCondition = getWatcherNameLikeCondition(watcherNameLike);

        Long count = select(watchingSession.count())
                .from(watchingSession)
                .innerJoin(profile).on(watchingSession.watcherId.eq(profile.accountId))
                .where(
                        watchingSession.liveChatId.eq(contentId),
                        watcherNameLikeCondition
                )
                .fetchOne();

        return count !=  null ? count : 0;
    }

    private static BooleanExpression keywordLikePredicate(String keywordLike){
        if (keywordLike.isEmpty()) {
            return null;
        }
        return content.title.containsIgnoreCase(keywordLike);
    }

    private static BooleanExpression typeEqualPredicate(String typeEqual){
        if (typeEqual.isEmpty()) {
            return null;
        }
        return content.type.eq(ContentType.valueOf(typeEqual));
    }

    private static BooleanExpression cursorExpressionPredicate(
            String cursor,
            UUID idAfter,
            SortDirection sortDirection,
            String sortBy
    ) {
        if (cursor == null || idAfter == null) {
            return null;
        }
        return switch (sortBy) {
            case "createdAt" -> {
                Instant createdAtCursor = Instant.parse(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.createdAt.gt(createdAtCursor)
                            .or(content.createdAt.eq(createdAtCursor).and(account.id.goe(idAfter)));
                }
                yield content.createdAt.lt(createdAtCursor)
                        .or(content.createdAt.eq(createdAtCursor).and(account.id.loe(idAfter)));
            }
            case "averageRating" -> {
                double averageRatingCursor = Double.parseDouble(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.averageRating.gt(averageRatingCursor)
                            .or(content.averageRating.eq(averageRatingCursor).and(account.id.goe(idAfter)));
                }
                yield content.averageRating.lt(averageRatingCursor)
                        .or(content.averageRating.eq(averageRatingCursor).and(account.id.loe(idAfter)));
            }
            default -> { // watcherCount : 인기순
                long watcherCountCursor = Long.parseLong(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.watcherCount.gt(watcherCountCursor) // TODO 이거맞나? content.watcherCount?
                            .or(content.watcherCount.eq(watcherCountCursor).and(content.id.goe(idAfter)));
                }
                yield content.watcherCount.lt(watcherCountCursor)
                        .or(content.watcherCount.eq(watcherCountCursor).and(content.id.loe(idAfter)));
            }
        };
    }

        private BooleanExpression getWatcherNameLikeCondition(String watcherName) {
        if (watcherName == null) {
            return null;
        }
        return profile.name.like("%" + watcherName + "%");
    }

    private BooleanExpression getCursorAndAssistanceCursorCondition(
            Instant cursor, UUID assistanceCursor, String direction
    ) {
        if (cursor == null || assistanceCursor == null) {
            return null;
        }

        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.createdAt.after(cursor).or(
                    watchingSession.createdAt.eq(cursor).and(
                            watchingSession.id.gt(assistanceCursor)
                    )
            );
        } else {
            return watchingSession.createdAt.before(cursor).or(
                    watchingSession.createdAt.eq(cursor).and(
                            watchingSession.id.lt(assistanceCursor)
                    )
            );
        }
    }

    private OrderSpecifier<Instant> getPrimaryOrder(String direction) {
        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.createdAt.asc();
        } else {
            return watchingSession.createdAt.desc();
        }
    }

    private OrderSpecifier<UUID> getSecondaryOrder(String direction) {
        if (direction.equalsIgnoreCase("ASCENDING")) {
            return watchingSession.id.asc();
        } else {
            return watchingSession.id.desc();
        }
    }

    private List<String> getTags(UUID contentId) {
        return select(content)
                .from(content)
                .leftJoin(content.tags).fetchJoin()
                .where(content.id.eq(contentId))
                .fetch()
                .stream()
                .flatMap(c -> c.getTags().stream().map(Tag::getName))
                .toList();
    }

//    private double calculateReviewAverage(long ratingSum, int reviewCount) {
//        if (reviewCount <= 0 || ratingSum <= 0) {
//            return 0.0;
//        }
//        return (double) ratingSum / reviewCount;
//    }

//    private WatchingSessionResponse combineSessionDetails(Tuple tuple, List<String> tags) {
//        UUID contentId = tuple.get(content.id);
//        long ratingSum = tuple.get(reviewStats.ratingSum);
//        int reviewCount = tuple.get(reviewStats.reviewCount);
//
//        return new WatchingSessionResponse(
//                tuple.get(watchingSession.id),
//                tuple.get(watchingSession.createdAt),
//                new UserSummaryDto(
//                        tuple.get(profile.accountId),
//                        tuple.get(profile.name),
//                        tuple.get(timeoutImage.presignedUrl)
//                ),
//                new ContentResult(
//                        contentId,
//                        tuple.get(content.type.stringValue()),
//                        tuple.get(content.title),
//                        tuple.get(content.description),
//                        tuple.get(content.thumbnailImage),
//                        tags,
//                        calculateReviewAverage(ratingSum, reviewCount),
//                        reviewCount
//                )
//        );
//    }

    private static OrderSpecifier<?>[] orderByExpressions(SortDirection sortDirection, String sortBy) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        orderSpecifiers.add(orderByCursor(sortDirection, sortBy));
        final var orderById = new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.id);
        orderSpecifiers.add(orderById);

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private static OrderSpecifier<?> orderByCursor(SortDirection sortDirection, String sortBy) {
        return switch (sortBy) {
            case "createdAt" -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.createdAt);
            case "averageRating" -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.averageRating);
            default -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.watcherCount);
        };
    }
}
