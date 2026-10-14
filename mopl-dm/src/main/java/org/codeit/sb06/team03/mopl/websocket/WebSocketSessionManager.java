package org.codeit.sb06.team03.mopl.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 서버에 물리적으로 연결된 WebSocketSession들을 메모리에 등록·보관하고, 연결 종료 시 제거
 */
@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }
}
