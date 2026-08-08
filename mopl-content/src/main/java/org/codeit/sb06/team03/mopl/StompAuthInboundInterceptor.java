package org.codeit.sb06.team03.mopl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.UserIdHandshakeInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Slf4j
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
            log.info("StompAuthInboundInterceptor CONNECT incoming request");
            // 1순위: Gateway가 HTTP Upgrade 요청에 주입한 X-User-Id (HandshakeInterceptor → session attributes)
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            String userId = sessionAttributes != null
                    ? (String) sessionAttributes.get(UserIdHandshakeInterceptor.USER_ID_ATTR)
                    : null;
            log.info("StompAuthInboundInterceptor 1st priority: userId={}", userId);


            if (userId == null || userId.isBlank()) {
                log.error("StompAuthInboundInterceptor failed: Missing user identity in CONNECT");
                throw new IllegalArgumentException("Missing user identity in WebSocket CONNECT");
            }

            final String finalUserId = userId;
            accessor.setUser((Principal) () -> finalUserId);
            log.info("StompAuthInboundInterceptor success: user set to {}", finalUserId);
        }

        return message;
    }

}
