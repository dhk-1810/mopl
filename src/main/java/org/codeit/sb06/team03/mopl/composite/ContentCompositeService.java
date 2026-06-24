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
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.image.application.in.RegisterImageUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final GetContentUseCase getContentUseCase;
    private final CreateContentUseCase createContentUseCase;
    private final UpdateContentUseCase updateContentUseCase;
    private final DeleteContentUseCase deleteContentUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;
    private final GetProfileUseCase getProfileUseCase;
    private final ContentTagService contentTagService;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;
    private final RegisterImageUseCase registerImageUseCase;

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        Slice<ContentReadModel> slice = getContentUseCase.getAll(request);
        List<ContentReadModel> readModels = slice.getContent();

        List<String> thumbnailKeys = readModels.stream()
                .map(ContentReadModel::thumbnailKey)
                .toList();
        Map<String, String> urls = getPresignedUrlUseCase.getPresignedUrls(thumbnailKeys);

        List<ContentDto> contents = readModels.stream()
                .map(rm -> ContentDto.from(rm, urls.get(rm.thumbnailKey())))
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

        ContentReadModel readModel = getContentUseCase.get(contentId);
        String presignedUrl = getPresignedUrlUseCase.getPresignedUrl(readModel.thumbnailKey());
        return ContentDto.from(readModel, presignedUrl);
    }

    public ContentDto create(ContentCreateRequest request, MultipartFile image) {

        String thumbnailKey = registerImageUseCase.register(image);
        ContentReadModel readModel = createContentUseCase.create(request, thumbnailKey);
        String presignedUrl = getPresignedUrlUseCase.getPresignedUrl(readModel.thumbnailKey());
        return ContentDto.from(readModel, presignedUrl);
    }

    public ContentDto update(UUID contentId, ContentUpdateRequest request) {

        ContentReadModel readModel = updateContentUseCase.update(contentId, request);
        String presignedUrl = getPresignedUrlUseCase.getPresignedUrl(readModel.thumbnailKey());
        return ContentDto.from(readModel, presignedUrl);
    }

    public void delete(UUID contentId) {
        deleteContentUseCase.delete(contentId);
    }


}

