package org.codeit.sb06.team03.mopl.content.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.ContentReadModel;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentsUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.GetSingleContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommandQueryService implements GetContentsUseCase, GetSingleContentUseCase {

    @Override
    public Slice<ContentReadModel> get(CursorRequestContentDto request) {
        return null;
    }

    @Override
    public ContentReadModel get(UUID contentId) {
        return null;
    }
}
