package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.application.PlaylistCommandService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContentEventListener {

    private final PlaylistCommandService playlistCommandService;

    @EventListener
    @Transactional
    public void handleContentDeleted(ContentDeletedEvent event) {
        log.info("Handling ContentDeletedEvent in mopl-playlist: {}", event);
        playlistCommandService.deleteCurationByContentId(event.contentId());
    }
}
