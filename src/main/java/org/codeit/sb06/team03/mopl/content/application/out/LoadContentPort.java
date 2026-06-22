package org.codeit.sb06.team03.mopl.content.application.out;

import org.codeit.sb06.team03.mopl.common.WatchingSessionResponse;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.SortContentBy;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LoadContentPort {

    Slice<ContentReadModel> findAll(
            String typeEqual,
            String keywordLike,
            Set<String> tagsIn, // TODO 확인 필요
            String cursor,
            UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    );

    Optional<Content> findByIdWithTags(UUID contentId);

    List<WatchingSessionResponse> findSessionsDetails(WatchingSessionCursorQuery query);

    long countByContentIdAndWatcherNameLike(UUID contentId, String watcherName);
}
