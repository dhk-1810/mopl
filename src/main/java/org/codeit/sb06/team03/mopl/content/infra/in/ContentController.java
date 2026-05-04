package org.codeit.sb06.team03.mopl.content.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.ContentCompositeService;
import org.codeit.sb06.team03.mopl.content.application.in.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.WatchingSessionCursorCommand;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApi {

    private final ContentCompositeService contentCompositeService;
    private final GetContentUseCase getContentUseCase; // TODO Composite로 대체

    @GetMapping
    public ResponseEntity<CursorResponseContentDto> getContents(@ModelAttribute CursorRequestContentDto request){

    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDto> getSingleContent(@PathVariable String contentId){
        return contentCompositeService.
    }

    @Override
    @GetMapping("/{contentId}/watching-sessions")
    public ResponseEntity<CursorResponseWatchingSessionDto> getWatchingSessions(
            @PathVariable String contentId,
            @ModelAttribute WatchingSessionCursorRequest watchingSessionCursorRequest
    ) {
        WatchingSessionCursorCommand watchingSessionCursorCommand =
                new WatchingSessionCursorCommand(
                        contentId,
                        watchingSessionCursorRequest.watcherNameLike(),
                        watchingSessionCursorRequest.cursor(),
                        watchingSessionCursorRequest.idAfter(),
                        watchingSessionCursorRequest.limit(),
                        watchingSessionCursorRequest.sortDirection(),
                        watchingSessionCursorRequest.sortBy()
                );

        CursorResponseWatchingSessionDto response = getContentUseCase.get(watchingSessionCursorCommand);

        return ResponseEntity.ok(response);
    }
}
