package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.contentTag.ContentTagService;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentQueryService implements GetContentUseCase {

    private final LoadContentPort loadContentPort;
    private final ContentTagService contentTagService;

    @Override
    public Slice<ContentReadModel> getAll(CursorRequestContentDto request) {

        return loadContentPort.findAll(
                request.typeEqual(),
                request.keywordLike(),
                request.tagsIn(),
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortBy(),
                request.sortDirection()
        );
    }


    @Override
    public ContentReadModel get(UUID contentId) {
        return loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
    }

    @Override
    public List<ContentReadModel> getByIds(Set<UUID> contentIds) {
        return loadContentPort.findByIdsIn(contentIds);
    }

}

