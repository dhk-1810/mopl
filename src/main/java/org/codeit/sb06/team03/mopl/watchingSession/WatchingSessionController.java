package org.codeit.sb06.team03.mopl.watchingSession;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.WatchingSessionDto;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.composite.WatchingSessionCompositeService;
import org.codeit.sb06.team03.mopl.content.application.in.CursorResponseWatchingSessionDto;
import org.codeit.sb06.team03.mopl.content.infra.in.CursorWatchingSessionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/")
public class WatchingSessionController {

    private final WatchingSessionCompositeService watchingSessionCompositeService;

    @GetMapping("/{watcherId}/watching-sessions")
    public ResponseEntity<WatchingSessionDto> getSessionDetails(
            @PathVariable UUID watcherId,
            @AuthenticationPrincipal MoplUserDetails userDetails
    ) {
        WatchingSessionDto sessionDetails = watchingSessionCompositeService
                .getWatchingSession(watcherId, userDetails);
        return ResponseEntity.ok(sessionDetails);
    }

    @GetMapping("/{contentId}/watching-sessions")
    public ResponseEntity<CursorResponseWatchingSessionDto> getByWatchingSessionId(
            @PathVariable UUID contentId,
            @ModelAttribute CursorWatchingSessionRequest watchingSessionCursorRequest
    ){
        CursorResponseWatchingSessionDto response = watchingSessionCompositeService
                .getWatchingSessions(contentId, watchingSessionCursorRequest);
        return ResponseEntity.ok(response);
    }
}
