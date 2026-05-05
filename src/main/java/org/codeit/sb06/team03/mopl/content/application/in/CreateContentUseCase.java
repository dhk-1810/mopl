package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.infra.in.ContentCreateRequest;

public interface CreateContentUseCase {

    ContentReadModel create(ContentCreateRequest request);

}
