package org.codeit.sb06.team03.mopl.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.enums.SortContentBy;
import org.codeit.sb06.team03.mopl.enums.ContentType;
import org.springframework.data.domain.*;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.codeit.sb06.team03.mopl.entity.QContent.content;
import static org.codeit.sb06.team03.mopl.entity.QContentTag.contentTag;
import static org.codeit.sb06.team03.mopl.entity.QTag.tag;
import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;

public interface ContentRepository extends QuerydslJpaRepository<Content, UUID> {

    boolean existsByTitleAndType(String title, ContentType type);

    default Optional<ContentReadModel> findByIdWithTags(UUID id) {

        // transform()은 Map을 반환하므로 단건이더라도 Map 사용. Key-Value는 한 쌍만 담김.
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
            @Nullable String typeEqual,
            @Nullable String keywordLike,
            @Nullable Set<String> tagsIn, // 미사용
            @Nullable String cursor,
            @Nullable UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    ) {
        Predicate[] predicates = {
                keywordLikePredicate(keywordLike),
                typeEqualPredicate(typeEqual),
                cursorExpressionPredicate(cursor, idAfter, sortBy, sortDirection)
        };

        List<UUID> contentIds = select(content.id)
                .from(content)
                .where(predicates)
                .orderBy(orderByExpressions(sortBy, sortDirection))
                .limit(limit + 1)
                .fetch();

        boolean hasNext = false;
        if (contentIds.size() > limit) {
            contentIds.remove(limit);
            hasNext = true;
        }

        // ReadModel 추출
        List<ContentReadModel> fetchedContents = findByIdsIn(contentIds);

        Map<UUID, ContentReadModel> contentMap = fetchedContents.stream()
                .collect(Collectors.toMap(ContentReadModel::id, c -> c));

        // 원래 순서로 정렬
        List<ContentReadModel> orderedContents = contentIds.stream()
                .map(contentMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new SliceImpl<>(orderedContents, PageRequest.ofSize(limit), hasNext);
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


    private static BooleanExpression keywordLikePredicate(@Nullable String keywordLike){
        if (keywordLike == null || keywordLike.isEmpty()) {
            return null;
        }
        return content.title.containsIgnoreCase(keywordLike);
    }

    private static BooleanExpression typeEqualPredicate(@Nullable String typeEqual){
        if (typeEqual == null || typeEqual.isEmpty()) {
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
                            .or(content.createdAt.eq(createdAtCursor).and(content.id.gt(idAfter)));
                }
                yield content.createdAt.lt(createdAtCursor)
                        .or(content.createdAt.eq(createdAtCursor).and(content.id.lt(idAfter)));
            }
            case SortContentBy.rate -> {
                double averageRatingCursor = Double.parseDouble(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.averageRating.gt(averageRatingCursor)
                            .or(content.averageRating.eq(averageRatingCursor).and(content.id.gt(idAfter)));
                }
                yield content.averageRating.lt(averageRatingCursor)
                        .or(content.averageRating.eq(averageRatingCursor).and(content.id.lt(idAfter)));
            }
            default -> { // watcherCount : 인기순
                long watcherCountCursor = Long.parseLong(cursor);
                if (SortDirection.ASCENDING == sortDirection) {
                    yield content.watcherCount.gt(watcherCountCursor)
                            .or(content.watcherCount.eq(watcherCountCursor).and(content.id.gt(idAfter)));
                }
                yield content.watcherCount.lt(watcherCountCursor)
                        .or(content.watcherCount.eq(watcherCountCursor).and(content.id.lt(idAfter)));
            }
        };
    }

    private static OrderSpecifier<?>[] orderByExpressions(SortContentBy sortBy, SortDirection sortDirection) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        orderSpecifiers.add(orderByCursor(sortBy, sortDirection));
        Order order = Order.DESC;
        if (sortDirection == SortDirection.ASCENDING) { order =  Order.ASC; }
        final var orderById = new OrderSpecifier<>(order, content.id);
        orderSpecifiers.add(orderById);

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private static OrderSpecifier<?> orderByCursor(SortContentBy sortBy, SortDirection sortDirection) {
        Order order = Order.DESC;
        if (sortDirection == SortDirection.ASCENDING) { order =  Order.ASC; }
        return switch (sortBy) {
            case SortContentBy.createdAt -> new OrderSpecifier<>(order, content.createdAt);
            case SortContentBy.watcherCount -> new OrderSpecifier<>(order, content.averageRating);
            default -> new OrderSpecifier<>(order, content.watcherCount);
        };
    }
}
