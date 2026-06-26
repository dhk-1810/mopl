package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.entity.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.domain.ContentTagService;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentCommand;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.DeleteContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentCommand;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.codeit.sb06.team03.mopl.content.domain.ContentService;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCommandService implements CreateContentUseCase, UpdateContentUseCase, DeleteContentUseCase {

    private final LoadContentPort loadContentPort;
    private final SaveContentPort saveContentPort;
    private final ContentService contentService;
    private final ContentTagService contentTagService;

    @Override
    public ContentReadModel create(CreateContentCommand command, String thumbnailKey) {
        Content content = contentService.create(
                command.type(), command.title(), command.description(), thumbnailKey);
        saveContentPort.save(content);

        Set<String> tags = contentTagService.create(content.getId(), command.tags());
        return ContentReadModel.from(content, tags);
    }

    @Override
    public ContentReadModel update(UUID contentId, UpdateContentCommand command) {
        Content content = loadContentPort.findById(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        contentService.update(content, command.title(), command.description());
        saveContentPort.save(content);

        Set<String> tags = contentTagService.create(content.getId(), command.tags());
        return ContentReadModel.from(content, tags);
    }

    @Override
    public void delete(UUID id) {
        Content content = loadContentPort.findById(id)
                .orElseThrow(() -> ContentNotFoundException.fromId(id));
        saveContentPort.deleteById(id);
    }
}

