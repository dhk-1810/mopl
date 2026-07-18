package org.codeit.sb06.team03.mopl.dto.response;
import org.codeit.sb06.team03.mopl.dto.request.ContentCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.ContentUpdateRequest;

import org.codeit.sb06.team03.mopl.service.application.CreateContentCommand;
import org.codeit.sb06.team03.mopl.service.application.UpdateContentCommand;
import org.springframework.stereotype.Component;

@Component
public class ContentMapper {

    public CreateContentCommand toCommand(ContentCreateRequest request) {
        return new CreateContentCommand(
                request.type(),
                request.title(),
                request.description(),
                request.tags()
        );
    }

    public UpdateContentCommand toCommand(ContentUpdateRequest request) {
        return new UpdateContentCommand(
                request.title(),
                request.description(),
                request.tags()
        );
    }

}
