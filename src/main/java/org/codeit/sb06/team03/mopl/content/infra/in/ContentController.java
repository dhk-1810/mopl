package org.codeit.sb06.team03.mopl.content.infra.in;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.ContentCompositeService;
import org.codeit.sb06.team03.mopl.content.application.in.GetContentUseCase;
import org.codeit.sb06.team03.mopl.content.infra.ContentDto;
import org.codeit.sb06.team03.mopl.content.infra.CursorRequestContentDto;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentCompositeService contentCompositeService;
    private final GetContentUseCase getContentUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;

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
    public ResponseEntity<ContentDto> create(
            @RequestBody ContentCreateRequest request,
            @RequestPart(name = "thumbnail") MultipartFile thumbnail
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentCompositeService.create(request, thumbnail));
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
}
