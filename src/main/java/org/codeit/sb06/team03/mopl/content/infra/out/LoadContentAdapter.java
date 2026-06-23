package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.SortContentBy;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadContentAdapter implements LoadContentPort {

    private final ContentRepository contentRepository;

    @Override
    public Slice<ContentReadModel> findAll(
            String typeEqual,
            String keywordLike,
            Set<String> tagsIn,
            String cursor,
            UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    ) {
        return contentRepository.findAll(
                typeEqual,
                keywordLike,
                tagsIn,
                cursor,
                idAfter,
                limit,
                sortBy,
                sortDirection
        );
    }

    @Override
    public Optional<Content> findByIdWithTags(UUID contentId) {
        return contentRepository.findByIdWithTags(contentId);
    }

    @Override
    public long countByContentIdAndWatcherNameLike(UUID contentId, String watcherName) {
        return contentRepository.countByContentIdAndWatcherNameLike(contentId, watcherName);
    }
}
