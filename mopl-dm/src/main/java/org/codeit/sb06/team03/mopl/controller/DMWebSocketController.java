package org.codeit.sb06.team03.mopl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.UserIdHandshakeInterceptor;
import org.codeit.sb06.team03.mopl.service.composite.DMCompositeService;
import org.codeit.sb06.team03.mopl.dto.request.MessageSendRequest;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DMWebSocketController {

    private final DMCompositeService dmCompositeService;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    public void sendMessage(
            @DestinationVariable UUID conversationId,
            @Payload MessageSendRequest request,
            Principal principal,
            SimpMessageHeaderAccessor accessor
    ) {
        log.info("DMWebSocketController.sendMessage called: conversationId={}, payload={}", conversationId, request);

        String userIdStr = principal != null ? principal.getName() : null;
        if ((userIdStr == null || userIdStr.isBlank()) && accessor != null) {
            if (accessor.getSessionAttributes() != null) {
                userIdStr = (String) accessor.getSessionAttributes().get(UserIdHandshakeInterceptor.USER_ID_ATTR);
            }
            if (userIdStr == null) userIdStr = accessor.getFirstNativeHeader("X-User-Id");
            if (userIdStr == null) userIdStr = accessor.getFirstNativeHeader("userId");
            if (userIdStr == null) userIdStr = accessor.getFirstNativeHeader("user-id");
        }

        if (userIdStr == null || userIdStr.isBlank()) {
            log.warn("Cannot send DM: missing user identity for conversationId={}", conversationId);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        dmCompositeService.sendDM(conversationId, userId, request);
    }
}
