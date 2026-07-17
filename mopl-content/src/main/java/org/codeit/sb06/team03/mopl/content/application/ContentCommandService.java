package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.entity.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.domain.ContentTagService;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentCommand;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentCommand;
import org.codeit.sb06.team03.mopl.content.domain.ContentService;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.infra.out.ContentRepository;
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

    public ContentReadModel create(CreateContentCommand command, String thumbnailKey) {
        Content content = contentService.create(
                command.type(), command.title(), command.description(), thumbnailKey);
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), command.tags());
        return ContentReadModel.from(content, tags);
    }

    public ContentReadModel update(UUID contentId, UpdateContentCommand command) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, command.title(), command.description());
        contentRepository.save(content);

        Set<String> tags = contentTagService.create(content.getId(), command.tags());
        return ContentReadModel.from(content, tags);
    }

    public void delete(UUID id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> ContentNotFoundException.fromId(id));
        contentRepository.deleteById(id);
    }
}

