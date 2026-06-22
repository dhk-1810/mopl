package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentsUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.GetSingleContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.out.LoadContentPort;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.watchingSession.application.out.LoadWatchingSessionPort;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ContentQueryService implements GetContentsUseCase, GetSingleContentUseCase {

    private final LoadContentPort loadContentPort;

    @Override
    public Slice<ContentReadModel> getAll(CursorRequestContentDto request) {

        loadContentPort.findAll(){

        }
    }

    @Override
    public ContentReadModel get(UUID contentId) {
        return null;
    }
}
