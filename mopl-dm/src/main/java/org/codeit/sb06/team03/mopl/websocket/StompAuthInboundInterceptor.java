package org.codeit.sb06.team03.mopl.websocket;

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
        if (command == StompCommand.CONNECT || command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {
            if (accessor.getUser() == null) {
                String userId = resolveUserId(accessor);
                if (userId != null && !userId.isBlank()) {
                    final String finalUserId = userId;
                    accessor.setUser((Principal) () -> finalUserId);
                    log.info("STOMP authenticated: userId={} for command={}", finalUserId, command);
                }
            }
        }

        return message;
    }

    private String resolveUserId(StompHeaderAccessor accessor) {
        // 1. HandshakeInterceptor 세션 특성
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            String userId = (String) sessionAttributes.get(UserIdHandshakeInterceptor.USER_ID_ATTR);
            if (userId != null && !userId.isBlank()) {
                return userId;
            }
        }

        // 2. STOMP Native Header X-User-Id / userId / user-id
        String nativeUserId = accessor.getFirstNativeHeader("X-User-Id");
        if (nativeUserId == null) nativeUserId = accessor.getFirstNativeHeader("userId");
        if (nativeUserId == null) nativeUserId = accessor.getFirstNativeHeader("user-id");
        if (nativeUserId != null && !nativeUserId.isBlank()) {
            return nativeUserId;
        }

        // 3. STOMP Native Header Authorization / passcode / token / access_token (JWT payload sub)
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token == null) token = accessor.getFirstNativeHeader("passcode");
        if (token == null) token = accessor.getFirstNativeHeader("token");
        if (token == null) token = accessor.getFirstNativeHeader("access_token");
        if (token != null && !token.isBlank()) {
            String extracted = UserIdHandshakeInterceptor.extractUserIdFromJwt(token);
            if (extracted != null && !extracted.isBlank()) {
                return extracted;
            }
        }

        return null;
    }
}
