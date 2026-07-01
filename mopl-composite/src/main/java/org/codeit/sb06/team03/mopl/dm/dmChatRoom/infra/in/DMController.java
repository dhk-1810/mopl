package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.DMCompositeService;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in.request.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dm_chat_rooms")
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
    @PostMapping("/{dmChatRoomId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> readDirectMessage(
            @PathVariable UUID dmChatRoomId,
            @PathVariable UUID directMessageId
    ) {
        dmCompositeService.readDM(dmChatRoomId, directMessageId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{dmChatRoomId}")
    public ResponseEntity<DMChatRoomDto> getDMChatRoom(@PathVariable UUID dmChatRoomId) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoom(dmChatRoomId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{dmChatRoomId}/direct-messages")
    public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @PathVariable UUID dmChatRoomId,
            @ModelAttribute CursorRequestDirectMessageDto request
    ) {
        CursorResponseDirectMessageDto response = dmCompositeService.getDMs(dmChatRoomId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/with")
    public ResponseEntity<DMChatRoomDto> getDMChatRoomWith(@RequestParam UUID partnerId) {
        DMChatRoomDto response = dmCompositeService.getDMChatRoomWith(partnerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
