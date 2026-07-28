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

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
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

            // 2순위: STOMP native 헤더 X-User-Id
            if (userId == null || userId.isBlank()) {
                userId = accessor.getFirstNativeHeader("X-User-Id");
                log.info("StompAuthInboundInterceptor 2nd priority: userId={}", userId);
            }

            // 3순위: Authorization: Bearer <jwt> 에서 sub 파싱 (브라우저 WebSocket은 Upgrade 시 커스텀 헤더 불가)
            if (userId == null || userId.isBlank()) {
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                userId = extractSubjectFromBearer(authHeader);
                log.info("StompAuthInboundInterceptor 3rd priority: userId={}", userId);
            }

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

    /**
     * JWT payload(Base64Url)를 디코딩해 "sub" 클레임을 추출한다.
     * Gateway에서 이미 서명 검증을 완료했으므로 여기서는 파싱만 수행한다.
     */
    private String extractSubjectFromBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String padded = parts[1];
            int rem = padded.length() % 4;
            if (rem != 0) padded += "=".repeat(4 - rem);

            byte[] decoded = Base64.getUrlDecoder().decode(padded);
            String json = new String(decoded, StandardCharsets.UTF_8);

            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            com.fasterxml.jackson.databind.JsonNode sub = node.get("sub");
            return sub != null && !sub.isNull() ? sub.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
