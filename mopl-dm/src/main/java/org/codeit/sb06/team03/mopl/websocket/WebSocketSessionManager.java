package org.codeit.sb06.team03.mopl.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }

    public void closeSessionForUser(String userId) {
        sessions.values().forEach(session -> {
            if (session.getPrincipal() != null && userId.equals(session.getPrincipal().getName())) {
                try {
                    session.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        });
    }
}
