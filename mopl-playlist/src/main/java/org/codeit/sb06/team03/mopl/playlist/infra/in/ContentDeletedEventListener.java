package org.codeit.sb06.team03.mopl.playlist.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.content.domain.event.ContentDeletedEvent;
import org.codeit.sb06.team03.mopl.playlist.application.in.DeleteCurationUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentDeletedEventListener {

    private final DeleteCurationUseCase deleteCurationUseCase;

    @EventListener
    public void handleContentDeleted(ContentDeletedEvent event) {
        deleteCurationUseCase.deleteCurationByContentId(event.contentId());
    }
}
