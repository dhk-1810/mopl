package org.codeit.sb06.team03.mopl.content.infra.in;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.ContentCompositeService;
import org.codeit.sb06.team03.mopl.content.application.in.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.application.in.WatchingSessionCursorCommand;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController implements ContentApi {

    private final ContentCompositeService contentCompositeService;
    private final GetContentUseCase getContentUseCase;

    @GetMapping
    public ResponseEntity<CursorResponseContentDto> getContents(@ModelAttribute CursorRequestContentDto request){
        return ResponseEntity.ok(contentCompositeService.getContents(request));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDto> getSingleContent(@PathVariable UUID contentId){
        return ResponseEntity.ok(contentCompositeService.getSingleContent(contentId));
    }

    @PostMapping
    @RolesAllowed("ADMIN")
    public ResponseEntity<ContentDto> create(@RequestBody ContentCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentCompositeService.create(request));
    }

    @PatchMapping("/{contentId}")
    @RolesAllowed("ADMIN")
    public ResponseEntity<ContentDto> update(
            @PathVariable UUID contentId,
            @RequestBody ContentUpdateRequest request
    ) {
        return ResponseEntity.ok(contentCompositeService.update(contentId, request));
    }

    @DeleteMapping
    @RolesAllowed("ADMIN")
    public ResponseEntity<Void> delete(@PathVariable UUID contentId){
        contentCompositeService.delete(contentId);
        return ResponseEntity.noContent().build();
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
