package org.codeit.sb06.team03.mopl.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.DMCompositeService;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.DMApi;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.DMChatRoomDto;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.request.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/conversations")
public class DMController implements DMApi {

    private final DMCompositeService dmCompositeService;

    @Override
    @GetMapping
    public ResponseEntity<CursorResponseDMChatRoomDto> getDMChatRooms(@ModelAttribute CursorRequestDMChatRoomDto request) {
        CursorResponseDMChatRoomDto response = dmCompositeService.getDMChatRooms(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<DMChatRoomDto> createDMChatRoom(@RequestBody(required = true) @Valid DMChatRoomCreateRequest request) {
        DMChatRoomDto response = dmCompositeService.createDMChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> readDirectMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID directMessageId
    ) {
        dmCompositeService.readDM(conversationId, directMessageId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{conversationId}")
    public ResponseEntity<DMChatRoomDto> getDMChatRoom(@PathVariable UUID conversationId) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoom(conversationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{conversationId}/direct-messages")
    public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @PathVariable UUID conversationId,
            @ModelAttribute CursorRequestDirectMessageDto request
    ) {
        CursorResponseDirectMessageDto response = dmCompositeService.getDMs(conversationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/with")
    public ResponseEntity<DMChatRoomDto> getDMChatRoomWith(@RequestParam UUID userId) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoomWith(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
