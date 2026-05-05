package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.springframework.data.domain.Slice;

public interface GetContentsUseCase {

    Slice<ContentReadModel> get(CursorRequestContentDto request);
}
