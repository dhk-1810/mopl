package org.codeit.sb06.team03.mopl.content.application.in;

import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GetContentUseCase {

    /**
     * 다건 조회
     */
    Slice<ContentReadModel> getAll(CursorRequestContentDto request);

    List<ContentReadModel> getByIds(Set<UUID> ids);

    /**
     단건 조회
     */
    ContentReadModel get(UUID contentId);
}
