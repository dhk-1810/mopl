package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionResponse;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.WatchingSessionCursorQuery;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadContentAdapter implements LoadContentPort {

    private final ContentRepository contentRepository;

    @Override
    public Slice<ContentReadModel> findAll(
            String typeEqual,
            String keywordLike,
            List<String> tagsIn,
            String cursor,
            UUID idAfter,
            int limit,
            SortDirection sortDirection,
            String sortBy
    ) {
        return contentRepository.findAll(
                typeEqual,
                keywordLike,
                tagsIn,
                cursor,
                idAfter,
                limit,
                sortDirection,
                sortBy
        );
    }

    @Override
    public Optional<Content> findByIdWithTags(UUID contentId) {
        return contentRepository.findByIdWithTags(contentId);
    }

    @Override
    public List<WatchingSessionResponse> findSessionsDetails(WatchingSessionCursorQuery query) {
        return contentRepository.findSessionsDetails(query);
    }

    @Override
    public long countByContentIdAndWatcherNameLike(UUID contentId, String watcherName) {
        return contentRepository.countByContentIdAndWatcherNameLike(contentId, watcherName);
    }
}
