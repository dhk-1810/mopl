package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateInternalRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentUpdateRequest;
import org.codeit.sb06.team03.mopl.entity.Content;
import org.codeit.sb06.team03.mopl.entity.ContentReadModel;
import org.codeit.sb06.team03.mopl.entity.ContentService;
import org.codeit.sb06.team03.mopl.entity.ContentTagService;
import org.codeit.sb06.team03.mopl.entity.SagaInstance;
import org.codeit.sb06.team03.mopl.event.ContentDeletionSagaEvent;
import org.codeit.sb06.team03.mopl.event.ContentEventPublisher;
import org.codeit.sb06.team03.mopl.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.repository.ContentRepository;
import org.codeit.sb06.team03.mopl.repository.SagaInstanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.codeit.sb06.team03.mopl.event.ContentUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional("contentTransactionManager")
public class ContentCommandService {

    private final ContentRepository contentRepository;
    private final SagaInstanceRepository sagaInstanceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ContentService contentService;
    private final ContentTagService contentTagService;
    private final ContentEventPublisher contentEventPublisher;

    public ContentReadModel create(ContentCreateRequest request, String thumbnailKey) {
        Content content = contentService.create(
                request.type(), request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        ContentReadModel readModel = ContentReadModel.from(content, tags);
        publishUpdatedEvent(readModel);
        return readModel;
    }

    public ContentReadModel create(UUID contentId, ContentCreateRequest request, String thumbnailKey) {
        Content content = contentService.create(
                contentId, request.type(), request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        ContentReadModel readModel = ContentReadModel.from(content, tags);
        publishUpdatedEvent(readModel);
        return readModel;
    }

    public ContentReadModel createInternal(ContentCreateInternalRequest request) {
        Content content = contentService.create(
                request.type(), request.title(), request.description(), request.thumbnailKey());
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        ContentReadModel readModel = ContentReadModel.from(content, tags);
        publishUpdatedEvent(readModel);
        return readModel;
    }

    private void publishUpdatedEvent(ContentReadModel readModel) {
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
    }

    public ContentReadModel update(UUID contentId, ContentUpdateRequest request, String thumbnailKey) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, request.title(), request.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), request.tags());
        ContentReadModel readModel = ContentReadModel.from(content, tags);
        publishUpdatedEvent(readModel);
        return readModel;
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
        SagaInstance sagaInstance = SagaInstance.create(sagaId, id);
        sagaInstanceRepository.save(sagaInstance);

        contentEventPublisher.publishContentDeletionSagaStart( // 여기서 Outbox 저장
                ContentDeletionSagaEvent.start(sagaId, id)
        );

        return sagaId;
    }

    public int updateWatcherCounts(java.util.Map<UUID, Long> watcherCounts) {
        if (watcherCounts == null || watcherCounts.isEmpty()) {
            return 0;
        }
        int totalUpdated = 0;
        for (java.util.Map.Entry<UUID, Long> entry : watcherCounts.entrySet()) {
            totalUpdated += contentRepository.updateWatcherCount(entry.getKey(), entry.getValue());
        }
        return totalUpdated;
    }
}

