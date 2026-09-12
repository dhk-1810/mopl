package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.entity.ContentService;
import org.codeit.sb06.team03.mopl.entity.ContentTagService;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("contentTransactionManager")
public class ContentCommandService {

    private final ContentRepository contentRepository;
    private final ContentService contentService;
    private final ContentTagService contentTagService;
    private final org.codeit.sb06.team03.mopl.event.ContentEventPublisher contentEventPublisher;

    public ContentReadModel create(ContentCreateRequest request, String thumbnailKey) {
        Content content = contentService.create(
                request.type(), request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        return ContentReadModel.from(content, tags);
    }

    public ContentReadModel create(UUID contentId, ContentCreateRequest request, String thumbnailKey) {
        Content content = contentService.create(
                contentId, request.type(), request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        return ContentReadModel.from(content, tags);
    }

    public ContentReadModel createInternal(ContentCreateInternalRequest request) {
        Content content = contentService.create(
                request.type(), request.title(), request.description(), request.thumbnailKey());
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        return ContentReadModel.from(content, tags);
    }

    public ContentReadModel update(UUID contentId, ContentUpdateRequest request, String thumbnailKey) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        return ContentReadModel.from(content, tags);
    }

    public void delete(UUID id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> ContentNotFoundException.fromId(id));
        content.markAsDeleted();
        contentRepository.save(content);
    }

    public UUID deleteSaga(UUID id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> ContentNotFoundException.fromId(id));

        // 1. Soft lock state
        content.markAsDeleting();
        contentRepository.save(content);

        // 2. Publish Saga Start Event
        UUID sagaId = UUID.randomUUID();
        contentEventPublisher.publishContentDeletionSagaStart(
                org.codeit.sb06.team03.mopl.event.ContentDeletionSagaEvent.start(sagaId, id)
        );

        return sagaId;
    }
}

