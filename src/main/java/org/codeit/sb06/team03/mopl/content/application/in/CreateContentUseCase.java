package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;

public interface CreateContentUseCase {

    ContentReadModel create(CreateContentCommand command, String thumbnailKey);

}
