package org.codeit.sb06.team03.mopl.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.Map;

/**
 * WebSocket Upgrade HTTP 요청에서 Gateway가 주입한 X-User-Id 헤더 또는 Query Param/Token을
 * WebSocket 세션 attributes에 저장한다.
 */
@Slf4j
@Component
public class UserIdHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "X-User-Id";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userId = request.getHeaders().getFirst(USER_ID_ATTR);
        if (userId == null || userId.isBlank()) {
            try {
                MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
                userId = queryParams.getFirst("X-User-Id");
                if (userId == null) userId = queryParams.getFirst("userId");
                if (userId == null) userId = queryParams.getFirst("user-id");
                if (userId == null) {
                    String token = queryParams.getFirst("token");
                    if (token == null) token = queryParams.getFirst("access_token");
                    if (token == null) token = queryParams.getFirst("accessToken");
                    if (token != null) {
                        userId = extractUserIdFromJwt(token);
                    }
                }
            } catch (Exception e) {
                log.debug("Error parsing query params during handshake: {}", e.getMessage());
            }
        }

        if (userId != null && !userId.isBlank()) {
            attributes.put(USER_ID_ATTR, userId);
            log.debug("WebSocket handshake resolved userId: {}", userId);
        } else {
            log.warn("WebSocket handshake: userId missing from upgrade request headers/query");
        }
        return true;
    }

    public static String extractUserIdFromJwt(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
                JsonNode node = OBJECT_MAPPER.readTree(bytes);
                if (node.has("sub")) {
                    return node.get("sub").asText();
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract sub from JWT token: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }
}
