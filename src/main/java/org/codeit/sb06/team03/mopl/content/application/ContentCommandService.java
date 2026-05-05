package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.CreateContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.DeleteContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.UpdateContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentCommandService implements CreateContentUseCase, UpdateContentUseCase, DeleteContentUseCase {

    @Override
    public ContentReadModel create(ContentCreateRequest request) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }

    @Override
    public ContentReadModel update(ContentUpdateRequest request) {
        return null;
    }
}
