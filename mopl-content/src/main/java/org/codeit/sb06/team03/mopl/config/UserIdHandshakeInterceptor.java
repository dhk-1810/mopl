package org.codeit.sb06.team03.mopl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Upgrade HTTP 요청에서 Gateway가 주입한 X-User-Id 헤더를
 * WebSocket 세션 attributes에 저장한다.
 * STOMP 레이어에서는 HTTP 헤더에 접근할 수 없으므로
 * 이 인터셉터를 통해 세션 attributes로 전달한다.
 */
@Slf4j
@Component
public class UserIdHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTR = "X-User-Id";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userId = request.getHeaders().getFirst(USER_ID_ATTR);
        if (userId != null && !userId.isBlank()) {
            attributes.put(USER_ID_ATTR, userId);
            log.debug("WebSocket handshake: X-User-Id={}", userId);
        } else {
            log.warn("WebSocket handshake: X-User-Id header missing from upgrade request");
        }
        return true;
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
