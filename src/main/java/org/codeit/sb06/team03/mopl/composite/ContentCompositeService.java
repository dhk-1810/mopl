package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionResponse;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.WatchingSessionCursorRequest;
import org.codeit.sb06.team03.mopl.playlist.PlaylistReadModel;
import org.codeit.sb06.team03.mopl.playlist.infra.in.response.UserSummaryDto;
import org.codeit.sb06.team03.mopl.user.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

        Slice<ContentReadModel> slice = getContentsUseCase.get(request);
        List<ContentReadModel> readModels = slice.getContent();
        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext()) {
            ContentReadModel lastItem = readModels.getLast();
            if (request.sortBy() == ); // TODO
            nextIdAfter = lastItem.id();
        }
        return new CursorResponseContentDto(
                contents,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                request.sortBy(),
                request.sortDirection()
        );
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

        // 워칭세션
        Slice<WatchingSessionReadModel> slice = getWatchingSessionUseCase.get(contentId, request);
        List<WatchingSessionReadModel> watchingSessions = slice.getContent();
        // 유저
        List<UUID> watcherIds = watchingSessions.stream().map(WatchingSessionReadModel::watcherId).toList();

        Map<UUID, UserSummaryDto> watchers = getProfileUseCase.getUserSummaries(watcherIds);
        // 컨텐트
        ContentReadModel content = getContentUseCase.get(contentId);

        List<WatchingSessionResponse> response = watchingSessions.stream()
                .map(rm -> new WatchingSessionResponse(rm, ));

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext()) {
            WatchingSessionResponse lastItem = response.getLast();
            nextCursor = lastItem.createdAt().toString();
            nextIdAfter = lastItem.id();
        }

        return new CursorResponseWatchingSessionDto(
                response,
                nextCursor,
                nextIdAfter,
                slice.hasNext(),
                0, // TODO
                SortDirection.valueOf(request.sortDirection()),
                request.sortBy()
        );
    }
}
