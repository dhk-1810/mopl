package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.UserSummary;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.WatchingSessionCursorRequest;
import org.codeit.sb06.team03.mopl.user.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.domain.WatchingSession;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final GetContentsUseCase getContentsUseCase;
    private final GetContentUseCase getContentUseCase;
    private final GetSingleContentUseCase getSingleContentUseCase;
    // 추가
    private final CreateContentUseCase createContentUseCase;
    private final UpdateContentUseCase updateContentUseCase;
    private final DeleteContentUseCase deleteContentUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;
    private final GetProfileUseCase getProfileUseCase;

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        Slice<ContentReadModel> contents = getContentsUseCase.get(request);
        return null;
    }

    public ContentDto getSingleContent(UUID contentId) {

        ContentReadModel readModel = getSingleContentUseCase.get(contentId);
        long watcherCount = getWatchingSessionUseCase.countByContentId(contentId);
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

    public CursorResponseWatchingSessionDto getWatchingSessions(UUID contentId, WatchingSessionCursorRequest request) {

        // 유저
        List<UserSummary> userSummary = getProfileUseCase.getUserSummaries();
        // 워칭세션
        List<WatchingSessionReadModel> watchingSessions = getWatchingSessionUseCase.get(contentId, request);
        // 컨텐트
        ContentReadModel content = getContentUseCase.get(contentId);

        CursorResponseWatchingSessionDto response ;
        return response;
    }
}
