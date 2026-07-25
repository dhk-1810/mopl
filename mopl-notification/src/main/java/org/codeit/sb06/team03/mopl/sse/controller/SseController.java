package org.codeit.sb06.team03.mopl.sse.controller;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.sse.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestParam(name = "LastEventID", required = false) UUID lastEventId
    ) {
        SseEmitter emitter = sseService.connect(userId, lastEventId);
        return ResponseEntity.ok(emitter);
    }
}
