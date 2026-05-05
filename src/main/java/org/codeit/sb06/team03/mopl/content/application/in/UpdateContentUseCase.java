package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentUpdateRequest;

public interface UpdateContentUseCase {

    ContentReadModel update(ContentUpdateRequest request);

}
