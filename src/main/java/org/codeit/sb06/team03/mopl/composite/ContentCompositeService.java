package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.*;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentMapper;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.image.application.in.RegisterImageUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.CreateLiveChatRoomUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.DeleteLiveChatRoomUseCase;
import org.codeit.sb06.team03.mopl.playlist.application.in.DeleteCurationUseCase;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final ContentMapper contentMapper;
    private final CreateContentUseCase createContentUseCase;
    private final GetContentUseCase getContentUseCase;
    private final UpdateContentUseCase updateContentUseCase;
    private final DeleteContentUseCase deleteContentUseCase;

    private final CreateLiveChatRoomUseCase createLiveChatRoomUseCase;
    private final DeleteLiveChatRoomUseCase deleteLiveChatRoomUseCase;

    private final GetPresignedUrlUseCase getPresignedUrlUseCase;
    private final RegisterImageUseCase registerImageUseCase;
    private final DeleteCurationUseCase deleteCurationUseCase;

    public ContentDto create(ContentCreateRequest request, MultipartFile image) {
        String thumbnailKey = registerImageUseCase.register(image);
        CreateContentCommand command = contentMapper.toCommand(request);
        ContentReadModel readModel = createContentUseCase.create(command, thumbnailKey);
        createLiveChatRoomUseCase.create(readModel.id());
        return ContentDto.from(readModel, getPresignedUrl(thumbnailKey));
    }

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        // 개발 편의를 위해 slice를 composite 계층까지 가져옴.
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
        return ContentDto.from(readModel, getPresignedUrl(readModel.thumbnailKey()));
    }

    public ContentDto update(UUID contentId, ContentUpdateRequest request) {
        UpdateContentCommand command = contentMapper.toCommand(request);
        ContentReadModel readModel = updateContentUseCase.update(contentId, command);
        return ContentDto.from(readModel, getPresignedUrl(readModel.thumbnailKey()));
    }

    public void delete(UUID contentId) {
        deleteContentUseCase.delete(contentId);
        deleteLiveChatRoomUseCase.delete(contentId);
        deleteCurationUseCase.deleteCurationByContentId(contentId); // TODO 오래걸리면 비동기?
    }

    private String getPresignedUrl(String thumbnailKey) {
        return getPresignedUrlUseCase.getPresignedUrl(thumbnailKey);
    }

}

