package org.codeit.sb06.team03.mopl.content.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.SortContentBy;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadContentAdapter implements LoadContentPort {

    private final ContentRepository repository;

    @Override
    public Slice<ContentReadModel> findAll(
            String typeEqual,
            String keywordLike,
            Set<String> tagsIn, // 미사용
            String cursor,
            UUID idAfter,
            int limit,
            SortContentBy sortBy,
            SortDirection sortDirection
    ) {
        return repository.findAll(
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
    public Optional<ContentReadModel> findByIdWithTags(UUID contentId) {
        return repository.findByIdWithTags(contentId);
    }

    @Override
    public Optional<Content> findById(UUID contentId) {
        return repository.findById(contentId);
    }

    @Override
    public List<ContentReadModel> findByIdsIn(Set<UUID> contentIds) {
        return repository.findByIdsIn(contentIds);
    }

    @Override
    public long countByContentIdAndWatcherNameLike(UUID contentId, String watcherName) {
        return repository.countByContentIdAndWatcherNameLike(contentId, watcherName);
    }
}
