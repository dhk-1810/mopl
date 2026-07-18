package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.ContentReadModel;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager", readOnly = true)
public class ContentQueryService {

    private final ContentRepository contentRepository;

    public Slice<ContentReadModel> getAll(CursorRequestContentDto request) {

        return contentRepository.findAll(
                request.typeEqual(),
                request.keywordLike(),
                request.tagsIn(), // 미사용
                request.cursor(),
                request.idAfter(),
                request.limit(),
                request.sortBy(),
                request.sortDirection()
        );
    }


    public ContentReadModel get(UUID contentId) {
        return contentRepository.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
    }

    public List<ContentReadModel> getByIds(Set<UUID> contentIds) {
        return contentRepository.findByIdsIn(contentIds);
    }

}

