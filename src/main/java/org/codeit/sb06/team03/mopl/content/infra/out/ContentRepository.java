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
import org.codeit.sb06.team03.mopl.content.domain.vo.ContentType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.*;

import static org.codeit.sb06.team03.mopl.account.domain.QAccount.account;
import static org.codeit.sb06.team03.mopl.content.QContent.content;
import static org.codeit.sb06.team03.mopl.contentTag.QContentTag.contentTag;
import static org.codeit.sb06.team03.mopl.profile.domain.QProfile.profile;
import static org.codeit.sb06.team03.mopl.watchingSession.domain.QWatchingSession.watchingSession;
import static org.codeit.sb06.team03.mopl.tag.entity.QTag.tag;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

public interface ContentRepository extends QuerydslJpaRepository<Content, UUID> {

    default Optional<ContentReadModel> findByIdWithTags(UUID id) {
        Map<UUID, ContentReadModel> resultMap = select(content)
                .from(content)
                .leftJoin(contentTag).on(content.id.eq(contentTag.id.contentId))
                .leftJoin(tag).on(contentTag.id.tagId.eq(tag.id))
                .where(content.id.eq(id))
                .transform(groupBy(content.id).as(Projections.constructor(ContentReadModel.class,
                        content.id,
                        content.type,
                        content.title,
                        content.description,
                        content.thumbnailKey,
                        set(tag.name),
                        content.averageRating,
                        content.reviewCount,
                        content.watcherCount,
                        content.createdAt
                )));

        return Optional.ofNullable(resultMap.get(id));
    }

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
                cursorExpressionPredicate(cursor, idAfter, sortBy, sortDirection)
        };

        var contents = select(Projections.constructor(ContentReadModel.class,
                    content.id,
                    content.type,
                    content.description,
                    content.thumbnailKey,
                    content.averageRating,
                    content.reviewCount,
                    content.watcherCount
                ))
                .from(content)
                .where(predicates)
                .orderBy(orderByExpressions(sortBy, sortDirection))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = false;
        if (contents.size() > limit) {
            contents.remove(limit);
            hasNext = true;
        }

        return new SliceImpl<>(contents, PageRequest.ofSize(limit), hasNext);
    }

    default List<ContentReadModel> findByIdsIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, ContentReadModel> resultMap = select(content)
                .from(content)
                .leftJoin(contentTag).on(content.id.eq(contentTag.id.contentId))
                .leftJoin(tag).on(contentTag.id.tagId.eq(tag.id))
                .where(content.id.in(ids))
                .transform(groupBy(content.id).as(Projections.constructor(ContentReadModel.class,
                        content.id,
                        content.type,
                        content.title,
                        content.description,
                        content.thumbnailKey,
                        set(tag.name),
                        content.averageRating,
                        content.reviewCount,
                        content.watcherCount,
                        content.createdAt
                )));

        return new ArrayList<>(resultMap.values());
    }

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
            SortContentBy sortBy,
            SortDirection sortDirection
    ) {
        if (cursor == null || idAfter == null) {
            return null;
        }
        return switch (sortBy) {
            case SortContentBy.createdAt -> {
                Instant createdAtCursor = Instant.parse(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.createdAt.gt(createdAtCursor)
                            .or(content.createdAt.eq(createdAtCursor).and(account.id.goe(idAfter)));
                }
                yield content.createdAt.lt(createdAtCursor)
                        .or(content.createdAt.eq(createdAtCursor).and(account.id.loe(idAfter)));
            }
            case SortContentBy.rate -> {
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
                    yield content.watcherCount.gt(watcherCountCursor)
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

    private static OrderSpecifier<?>[] orderByExpressions(SortContentBy sortBy, SortDirection sortDirection) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        orderSpecifiers.add(orderByCursor(sortBy, sortDirection));
        final var orderById = new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.id);
        orderSpecifiers.add(orderById);

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private static OrderSpecifier<?> orderByCursor(SortContentBy sortBy, SortDirection sortDirection) {
        return switch (sortBy) {
            case SortContentBy.createdAt -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.createdAt);
            case SortContentBy.watcherCount -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.averageRating);
            default -> new OrderSpecifier<>(Order.valueOf(sortDirection.toString()), content.watcherCount);
        };
    }
}
