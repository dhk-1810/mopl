package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.websocket.WebSocketSessionManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenExpiredEventListener {

    private final WebSocketSessionManager webSocketSessionManager;

    @RabbitListener(queues = RabbitConfig.TOKEN_EXPIRED_QUEUE)
    public void handleTokenExpired(TokenExpiredEvent event) {
        log.info("Received TokenExpiredEvent from RabbitMQ in mopl-dm: {}", event);
        if (event.userId() != null) {
            webSocketSessionManager.closeSessionForUser(event.userId().toString());
            log.info("Successfully closed WebSocket sessions for userId: {}", event.userId());
        }
    }
}
