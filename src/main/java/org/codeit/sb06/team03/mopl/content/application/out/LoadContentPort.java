package org.codeit.sb06.team03.mopl.content.application.out;

import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.SortContentBy;
import org.springframework.data.domain.Slice;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LoadContentPort {

    Slice<ContentReadModel> findAll(
            @Nullable String typeEqual,
            @Nullable String keywordLike,
            @Nullable Set<String> tagsIn, // 미사용
            @Nullable String cursor,
            @Nullable UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    );

    Optional<ContentReadModel> findByIdWithTags(UUID contentId);

    Optional<Content> findById(UUID contentId);

    List<ContentReadModel> findByIdsIn(Set<UUID> contentIds);

    long countByContentIdAndWatcherNameLike(UUID contentId, @Nullable String watcherName);
}
