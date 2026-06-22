package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.Content;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.DeleteContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.application.out.SaveContentPort;
import org.codeit.sb06.team03.mopl.content.domain.entity.Tag;
import org.codeit.sb06.team03.mopl.content.domain.exception.ContentNotFoundException;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCommandService implements CreateContentUseCase, UpdateContentUseCase, DeleteContentUseCase {

    private final LoadContentPort loadContentPort;
    private final SaveContentPort saveContentPort;

    @Override
    public ContentReadModel create(ContentCreateRequest request, String thumbnailUrl) {

        Set<Tag> tags = Collections.emptySet(); // TODO
        Content content = Content.create(
                request.type(),
                request.title(),
                request.description(),
                tags,
                thumbnailUrl
        );
        saveContentPort.save(content);
        return ContentReadModel.from(content);
    }

    @Override
    public ContentReadModel update(UUID contentId, ContentUpdateRequest request) {

        Content content = loadContentPort.findByIdWithTags(contentId)
                .orElseThrow(() -> ContentNotFoundException.fromId(contentId));
        saveContentPort.save(content);
        return ContentReadModel.from(content);
    }

    @Override
    public void delete(UUID id) {
        saveContentPort.deleteById(id);
    }
}
