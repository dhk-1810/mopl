package org.codeit.sb06.team03.mopl.service.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.service.application.*;
import org.codeit.sb06.team03.mopl.event.ContentDeletedEvent;
import org.codeit.sb06.team03.mopl.event.ContentUpdatedEvent;
import org.codeit.sb06.team03.mopl.dto.response.ContentDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.response.ContentMapper;
import org.codeit.sb06.team03.mopl.dto.request.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseContentDto;
import org.codeit.sb06.team03.mopl.s3.S3Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.codeit.sb06.team03.mopl.event.ImageUploadEvent;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final ContentMapper contentMapper;
    private final ContentCommandService contentCommandService;
    private final ContentQueryService contentQueryService;

    private final LiveChatRoomCommandService liveChatRoomCommandService;

    private final ExternalImageQueryService imageQueryService;
    private final ApplicationEventPublisher eventPublisher;
    
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    private static final String IMAGE_EXCHANGE = "mopl.image.exchange";
    private static final String IMAGE_ROUTING_KEY = "mopl.image.upload";

    public ContentDto createInternal(ContentCreateInternalRequest request) {
        CreateContentCommand command = new CreateContentCommand(request.type(), request.title(), request.description(), request.tags());
        ContentReadModel readModel = contentCommandService.create(command, request.thumbnailKey());
        liveChatRoomCommandService.create(readModel.id());

        return ContentDto.from(readModel, getPresignedUrl(request.thumbnailKey()));
    }

    public ContentDto create(ContentCreateRequest request, MultipartFile image) {
        String thumbnailKey = null;
        if (image != null && !image.isEmpty()) {
            thumbnailKey = "contents/" + UUID.randomUUID().toString();
            try {
                // 1. 직접 S3 업로드
                s3Service.uploadFile(thumbnailKey, image);

                // 2. RabbitMQ로 이미지 서비스에 메타데이터 생성 위임
                ImageUploadEvent event = new ImageUploadEvent(thumbnailKey, image.getContentType());
                rabbitTemplate.convertAndSend(IMAGE_EXCHANGE, IMAGE_ROUTING_KEY, event);
            } catch (IOException e) {
                throw new RuntimeException("S3 direct upload failed in content-service", e);
            }
        }

        CreateContentCommand command = contentMapper.toCommand(request);
        ContentReadModel readModel = contentCommandService.create(command, thumbnailKey);
        liveChatRoomCommandService.create(readModel.id());
        
        return ContentDto.from(readModel, getPresignedUrl(thumbnailKey));
    }

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

        // 개발 편의를 위해 slice를 composite 계층까지 가져옴.
        Slice<ContentReadModel> slice = contentQueryService.getAll(request);
        List<ContentReadModel> readModels = slice.getContent();

        List<String> thumbnailKeys = readModels.stream()
                .map(ContentReadModel::thumbnailKey)
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(thumbnailKeys);

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
        ContentReadModel readModel = contentQueryService.get(contentId);
        return ContentDto.from(readModel, getPresignedUrl(readModel.thumbnailKey()));
    }

    public ContentDto update(UUID contentId, ContentUpdateRequest request) {
        UpdateContentCommand command = contentMapper.toCommand(request);
        ContentReadModel readModel = contentCommandService.update(contentId, command);
        
        eventPublisher.publishEvent(new ContentUpdatedEvent(
                readModel.id(),
                readModel.type(),
                readModel.title(),
                readModel.description(),
                readModel.thumbnailKey(),
                readModel.tags(),
                readModel.averageRating(),
                readModel.reviewCount(),
                readModel.watcherCount()
        ));
        
        return ContentDto.from(readModel, getPresignedUrl(readModel.thumbnailKey()));
    }

    public void delete(UUID contentId) {
        contentCommandService.delete(contentId);
        liveChatRoomCommandService.delete(contentId);
        eventPublisher.publishEvent(new ContentDeletedEvent(contentId));
    }

    private String getPresignedUrl(String thumbnailKey) {
        return imageQueryService.getPresignedUrl(thumbnailKey);
    }
}
