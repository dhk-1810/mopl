package org.codeit.sb06.team03.mopl.dm.conversation.infra.in;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.DMCompositeService;
import org.codeit.sb06.team03.mopl.dm.conversation.infra.in.request.*;
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
    public ResponseEntity<CursorResponseConversationDto> getConversations(@ModelAttribute CursorRequestConversationDto request) {
        CursorResponseConversationDto response = dmCompositeService.getConversations(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<ConversationDto> createConversation(@RequestBody(required = true) @Valid ConversationCreateRequest request) {
        ConversationDto response = dmCompositeService.createConversation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping("/{conversationId}/direct-messages/{directMessageId}/read")
    public ResponseEntity<Void> readDirectMessage(
            @PathVariable UUID conversationId,
            @PathVariable UUID directMessageId
    ) {
        dmCompositeService.readDirectMessage(conversationId, directMessageId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/{conversationId}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable UUID conversationId) {
        ConversationDto response = dmCompositeService.getConversation(conversationId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{conversationId}/direct-messages")
    public ResponseEntity<CursorResponseDirectMessageDto> getDirectMessages(
            @PathVariable UUID conversationId,
            @ModelAttribute CursorRequestDirectMessageDto request
    ) {
        CursorResponseDirectMessageDto response = dmCompositeService.getDirectMessages(conversationId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/with")
    public ResponseEntity<ConversationDto> getConversationWith(@RequestParam UUID partnerId) {
        ConversationDto response = dmCompositeService.getConversationWith(partnerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
