package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.DMChatRoomDto;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDirectMessageDto;
import org.codeit.sb06.team03.mopl.dto.request.DMChatRoomCreateRequest;
import org.codeit.sb06.team03.mopl.dto.request.CursorRequestDMChatRoomDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.composite.DMCompositeService;
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
    public ResponseEntity<CursorResponseDMChatRoomDto> getDMChatRooms(
            @ModelAttribute CursorRequestDMChatRoomDto request,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        CursorResponseDMChatRoomDto response = dmCompositeService.getDMChatRooms(request, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<DMChatRoomDto> createDMChatRoom(
            @RequestBody(required = true) @Valid DMChatRoomCreateRequest request,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        DMChatRoomDto response = dmCompositeService.createDMChatRoom(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> readDirectMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID directMessageId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        dmCompositeService.readDM(conversationId, directMessageId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{conversationId}")
    public ResponseEntity<DMChatRoomDto> getDMChatRoom(
            @PathVariable UUID conversationId,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoom(conversationId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{conversationId}/direct-messages")
    public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @PathVariable UUID conversationId,
            @ModelAttribute CursorRequestDirectMessageDto request,
            @RequestHeader(value = "X-User-Id") UUID userId
    ) {
        CursorResponseDirectMessageDto response = dmCompositeService.getDMs(conversationId, request, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/with")
    public ResponseEntity<DMChatRoomDto> getDMChatRoomWith(
            @RequestParam UUID userId,
            @RequestHeader(value = "X-User-Id") UUID currentUserId
    ) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoomWith(userId, currentUserId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
