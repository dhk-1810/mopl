package org.codeit.sb06.team03.mopl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompAuthInboundInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        if (command == StompCommand.CONNECT) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            if (userId == null || userId.isBlank()) {
                throw new IllegalArgumentException("Missing X-User-Id header");
            }
            Principal principal = () -> userId;
            accessor.setUser(principal);
        }

        return message;
    }
}
