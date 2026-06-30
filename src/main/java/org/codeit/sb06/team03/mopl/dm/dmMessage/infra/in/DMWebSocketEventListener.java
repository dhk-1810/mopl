package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.JoinDMMessageCommand;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.JoinDMMessageUseCase;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.LeaveDMMessageCommand;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.LeaveDMMessageUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class DMWebSocketEventListener {

    private static final Pattern DM_SUB_PATTERN = Pattern.compile("^/sub/dm_chat_rooms/[0-9a-fA-F-]+/direct-messages$");

    private final JoinDMMessageUseCase joindmMessageUseCase;
    private final LeaveDMMessageUseCase leaveDMMessageUseCase;

    @EventListener
    void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = accessor.getDestination();
        if (destination == null || !DM_SUB_PATTERN.matcher(destination).matches()) return;
        if (event.getUser() == null) return;

        UUID dmChatRoomId = extractDMChatRoomId(destination);
        UUID userId = getUserId(event.getUser());

        joindmMessageUseCase.join(new JoinDMMessageCommand(dmChatRoomId, userId));
    }

    @EventListener
    void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = (String) accessor.getSessionAttributes().get(accessor.getSubscriptionId());
        if (destination == null || !DM_SUB_PATTERN.matcher(destination).matches()) return;

        accessor.getSessionAttributes().remove(accessor.getSubscriptionId());

        if (event.getUser() == null) return;

        UUID dmChatRoomId = extractDMChatRoomId(destination);
        UUID userId = getUserId(event.getUser());

        leaveDMMessageUseCase.leave(new LeaveDMMessageCommand(dmChatRoomId, userId));
    }

    @EventListener
    void onDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;
        if (event.getUser() == null) return;
        if (accessor.getSessionAttributes() == null || accessor.getSessionAttributes().isEmpty()) return;

        UUID userId = getUserId(event.getUser());

        List<String> dmDestinations = accessor.getSessionAttributes().values().stream()
                .filter(v -> v instanceof String)
                .map(v -> (String) v)
                .filter(d -> DM_SUB_PATTERN.matcher(d).matches())
                .toList();

        dmDestinations.forEach(destination -> {
            UUID dmChatRoomId = extractDMChatRoomId(destination);
            leaveDMMessageUseCase.leave(new LeaveDMMessageCommand(dmChatRoomId, userId));
        });
    }

    private UUID extractDMChatRoomId(String destination) {
        String[] parts = destination.split("/");
        return UUID.fromString(parts[3]);
    }

    private UUID getUserId(Principal principal) {
        Authentication authentication = (Authentication) principal;
        MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}