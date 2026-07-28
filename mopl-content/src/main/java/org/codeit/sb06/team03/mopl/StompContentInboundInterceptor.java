package org.codeit.sb06.team03.mopl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompContentInboundInterceptor implements ChannelInterceptor {

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

        return switch (command) {
            case SEND -> handleSend(accessor, message);
            case SUBSCRIBE -> handleSubscribe(accessor, message);
            default -> message;
        };
    }

    private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        log.info("StompContentInboundInterceptor handleSend destination={}, user={}", destination, accessor.getUser());

        if (destination == null) {
            log.error("StompContentInboundInterceptor handleSend failed: destination is null");
            throw new SecurityException("잘못된 전송 경로 입니다.");
        }
        if (DestinationUtils.isContentDestination(destination)) {
            if (!DestinationUtils.matchPubDestination(destination)) {
                log.error("StompContentInboundInterceptor handleSend failed: matchPubDestination mismatch for destination={}", destination);
                throw new SecurityException("잘못된 전송 경로 입니다.");
            }
        }

        log.info("StompContentInboundInterceptor handleSend success");
        return message;
    }

    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        String destination = accessor.getDestination();
        log.info("StompContentInboundInterceptor handleSubscribe destination={}, user={}", destination, accessor.getUser());

        if (destination == null) {
            log.error("StompContentInboundInterceptor handleSubscribe failed: destination is null");
            throw new SecurityException("잘못된 구독 경로 입니다.");
        }

        if (DestinationUtils.isContentDestination(destination)) {
            boolean isWatch = DestinationUtils.matchWatchSubDestination(destination);
            boolean isChat = DestinationUtils.matchChatSubDestination(destination);
            log.info("StompContentInboundInterceptor handleSubscribe check: isWatch={}, isChat={}", isWatch, isChat);
            if (!isWatch && !isChat) {
                log.error("StompContentInboundInterceptor handleSubscribe failed: destination matches neither watch nor chat pattern");
                throw new SecurityException("잘못된 구독 경로 입니다.");
            }
        }

        accessor.getSessionAttributes().put(accessor.getSubscriptionId(), destination);
        log.info("StompContentInboundInterceptor handleSubscribe success: saved subscriptionId={} for destination={}", accessor.getSubscriptionId(), destination);

        return message;
    }
}
