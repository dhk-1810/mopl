package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final GetContentsUseCase getContentsUseCase;
    private final GetSingleContentUseCase getSingleContentUseCase;
    // 추가
    private final CreateContentUseCase createContentUseCase;
    private final UpdateContentUseCase updateContentUseCase;
    private final DeleteContentUseCase deleteContentUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        Slice<ContentReadModel> contents = getContentsUseCase.get(request);
        return null;
    }

    public ContentDto getSingleContent(UUID contentId) {

        ContentReadModel readModel = getSingleContentUseCase.get(contentId);
//        long watcherCount = getWatchingSessionUseCase.getByContentId();
        return ContentDto.from(readModel, 0);
    }

    public ContentDto create(ContentCreateRequest request) {

        ContentReadModel readModel = createContentUseCase.create(request);
//        long watcherCount = getWatchingSessionUseCase.getByContentId();
        return ContentDto.from(readModel, 0);
    }

    public ContentDto update(UUID contentId, ContentUpdateRequest request) {

        ContentReadModel readModel = updateContentUseCase.update(request);
//        long watcherCount = getWatchingSessionUseCase.getByContentId();
        return ContentDto.from(readModel, 0);
    }

    public void delete(UUID contentId) {
        deleteContentUseCase.delete(contentId);
    }

}
