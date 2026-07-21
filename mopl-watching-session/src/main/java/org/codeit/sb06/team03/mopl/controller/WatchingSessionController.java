package org.codeit.sb06.team03.mopl.controller;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dto.response.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.service.composite.WatchingSessionCompositeService;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorWatchingSessionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class WatchingSessionController {

    private final WatchingSessionCompositeService watchingSessionCompositeService;

    @GetMapping("/users/{watcherId}/watching-sessions")
    public ResponseEntity<WatchingSessionDto> getByWatcherId(
            @PathVariable UUID watcherId,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        WatchingSessionDto sessionDetails = watchingSessionCompositeService
                .getByWatcherId(watcherId, userId);
        return ResponseEntity.ok(sessionDetails);
    }

    @GetMapping("/contents/{contentId}/watching-sessions")
    public ResponseEntity<CursorResponseWatchingSessionDto> getByContentId(
            @PathVariable UUID contentId,
            @ModelAttribute CursorWatchingSessionRequest watchingSessionCursorRequest
    ){
        CursorResponseWatchingSessionDto response = watchingSessionCompositeService
                .getByContentId(contentId, watchingSessionCursorRequest);
        return ResponseEntity.ok(response);
    }
}
