package org.codeit.sb06.team03.mopl.composite;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.application.in.GetSingleContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorResponseContentDto;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ContentCompositeService {

    private final GetContentsUseCase getContentsUseCase;
    private final GetSingleContentUseCase getSingleContentUseCase;

    public CursorResponseContentDto getContents(CursorRequestContentDto request) {

    }

    public ContentDto getSingleContent(String contentId) {

    }

}
