package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;

import java.util.UUID;

public interface GetSingleContentUseCase {

    ContentReadModel get(UUID contentId);

}
