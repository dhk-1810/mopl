package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.contentTag.ContentTagService;
import org.codeit.sb06.team03.mopl.tag.entity.Tag;
import org.codeit.sb06.team03.mopl.user.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

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
    private final ContentTagService contentTagService;

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        Slice<ContentReadModel> slice = getContentsUseCase.getAll(request);
        List<ContentReadModel> readModels = slice.getContent();

        List<ContentDto> contents = readModels.stream()
                .map(rm -> ContentDto.from(rm, rm.watcherCount()))
                .toList();

        String nextCursor = null;
        UUID nextIdAfter = null;
        if (slice.hasNext()) {
            ContentReadModel lastItem = readModels.getLast();
            nextCursor = switch (request.sortBy()) {
                case createdAt -> lastItem.createdAt().toString();
                case watcherCount -> String.valueOf(lastItem.watcherCount());
                case rate -> String.valueOf(lastItem.averageRating());
            };
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
        long watcherCount = getWatchingSessionUseCase.countByContentId(contentId); // TODO Content에 watcherCount 역정규화?
        return ContentDto.from(readModel, watcherCount);
    }

    public ContentDto create(ContentCreateRequest request, MultipartFile image) {

        ContentReadModel readModel = createContentUseCase.create(request, image);
        return ContentDto.from(readModel, 0);
    }

    public ContentDto update(UUID contentId, ContentUpdateRequest request) {

        ContentReadModel readModel = updateContentUseCase.update(contentId, request);
        long watcherCount = getWatchingSessionUseCase.countByContentId(contentId);
        return ContentDto.from(readModel, watcherCount);
    }

    public void delete(UUID contentId) {
        deleteContentUseCase.delete(contentId);
    }


}
