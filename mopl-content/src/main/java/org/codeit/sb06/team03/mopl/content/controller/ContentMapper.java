package org.codeit.sb06.team03.mopl.content.controller;

import org.codeit.sb06.team03.mopl.content.service.CreateContentCommand;
import org.codeit.sb06.team03.mopl.content.service.UpdateContentCommand;
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
