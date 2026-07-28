package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.DestinationUtils;
import org.codeit.sb06.team03.mopl.enums.WatchType;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.service.application.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveChatRoomWebEventListener {

    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ExternalImageQueryService imageQueryService;
    private final RabbitTemplate rabbitTemplate;

    private static final String WS_EXCHANGE = "watching-session.exchange";
    private static final String WS_CREATE_ROUTING = "watching-session.create";
    private static final String WS_DELETE_ROUTING = "watching-session.delete";

    // 같은 채널을 구독하지 못하게 하는 로직 필요
    @EventListener
    void onLiveChatRoomSubscribedEvent(SessionSubscribeEvent event) {
        log.info("onLiveChatRoomSubscribedEvent triggered: user={}", event.getUser());

        if (event.getUser() == null) {
            log.warn("onLiveChatRoomSubscribedEvent aborted: user is null");
            return;
        }

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) {
            log.warn("onLiveChatRoomSubscribedEvent aborted: accessor is null");
            return;
        }

        String destination = accessor.getDestination();
        log.info("onLiveChatRoomSubscribedEvent destination: {}, subscriptionId: {}", destination, accessor.getSubscriptionId());
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) {
            log.info("onLiveChatRoomSubscribedEvent ignored: destination does not match watch pattern");
            return;
        }

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatRoomId = contentId; // LiveChatRoom은 Content와 같은 ID를 쓰고 있음.

        UUID userId = getUserId(event.getUser());
        ExternalUserView userView = externalUserQueryService.getProfile(userId);
        String name = userView != null ? userView.getName() : "Unknown User";
        String imageKey = userView != null ? userView.getProfileImageKey() : null;

        UUID sessionId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // 1. RabbitMQ를 통해 mopl-watching-session에 세션 생성을 위임
        log.info("Sending WatchingSessionCreateRequestEvent: sessionId={}, liveChatRoomId={}, userId={}", sessionId, liveChatRoomId, userId);
        WatchingSessionCreateRequestEvent createEvent = new WatchingSessionCreateRequestEvent(
                sessionId, liveChatRoomId, userId, createdAt
        );
        rabbitTemplate.convertAndSend(WS_EXCHANGE, WS_CREATE_ROUTING, createEvent);

        String profileImageUrl = imageQueryService.getPresignedUrl(imageKey);
        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        sessionId,
                        createdAt,
                        userId,
                        name,
                        profileImageUrl,
                        WatchType.JOIN.name(),
                        destination
                );
        log.info("Sending Presence Message: liveChatRoomId={}, userId={}, name={}", liveChatRoomId, userId, name);
        liveChatRoomCommandService.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatRoomUnSubscribedEvent(SessionUnsubscribeEvent event) {
        log.info("onLiveChatRoomUnSubscribedEvent triggered: user={}", event.getUser());

        if (event.getUser() == null) {
            log.warn("onLiveChatRoomUnSubscribedEvent aborted: user is null");
            return;
        }

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) {
            log.warn("onLiveChatRoomUnSubscribedEvent aborted: accessor is null");
            return;
        }

        String destination = (String) accessor.getSessionAttributes().get(accessor.getSubscriptionId());
        log.info("onLiveChatRoomUnSubscribedEvent destination from sessionAttributes: {}", destination);
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) {
            log.info("onLiveChatRoomUnSubscribedEvent ignored: destination does not match watch pattern");
            return;
        }

        accessor.getSessionAttributes().remove(accessor.getSubscriptionId());

        UUID userId = getUserId(event.getUser());
        ExternalUserView userView = externalUserQueryService.getProfile(userId);
        String name = userView != null ? userView.getName() : "Unknown User";
        String imageKey = userView != null ? userView.getProfileImageKey() : null;

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatRoomId = contentId; // LiveChatRoom은 Content와 같은 ID를 쓰고 있음.

        // 2. RabbitMQ를 통해 mopl-watching-session에 세션 삭제를 위임
        log.info("Sending WatchingSessionDeleteRequestEvent for unsubscribe: userId={}", userId);
        WatchingSessionDeleteRequestEvent deleteEvent = new WatchingSessionDeleteRequestEvent(null, userId);
        rabbitTemplate.convertAndSend(WS_EXCHANGE, WS_DELETE_ROUTING, deleteEvent);

        String profileImageUrl = imageQueryService.getPresignedUrl(imageKey);
        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        UUID.randomUUID(),
                        Instant.now(),
                        userId,
                        name,
                        profileImageUrl,
                        WatchType.LEAVE.name(),
                        destination
                );

        log.info("Sending Presence Message (LEAVE) for unsubscribe: liveChatRoomId={}, userId={}", liveChatRoomId, userId);
        liveChatRoomCommandService.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatRoomDisconnectedEvent(SessionDisconnectEvent event) {
        log.info("onLiveChatRoomDisconnectedEvent triggered: user={}", event.getUser());

        if (event.getUser() == null) {
            log.info("onLiveChatRoomDisconnectedEvent aborted: user is null (probably disconnected before connect completed)");
            return; // user가 null인 경우는 connect에서 setUser를 하기 전에 종료되었을 때 뿐임, 그러므로 데이터베이스 작업은 불필요함
        }

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) {
            log.warn("onLiveChatRoomDisconnectedEvent aborted: accessor is null");
            return;
        }
        if (accessor.getSessionAttributes() == null || accessor.getSessionAttributes().isEmpty()) {
            log.info("onLiveChatRoomDisconnectedEvent ignored: sessionAttributes is empty");
            return;
        }

        UUID userId = getUserId(event.getUser());
        ExternalUserView userView = externalUserQueryService.getProfile(userId);
        String name = userView != null ? userView.getName() : "Unknown User";
        String imageKey = userView != null ? userView.getProfileImageKey() : null;

        List<String> destinations = accessor.getSessionAttributes().values()
                .stream().map(value -> (String) value)
                .filter(DestinationUtils::matchWatchSubDestination)
                .toList();

        log.info("onLiveChatRoomDisconnectedEvent active destinations: {}", destinations);
        if (destinations.isEmpty()) return;

        // 3. RabbitMQ를 통해 mopl-watching-session에 세션 삭제를 위임
        log.info("Sending WatchingSessionDeleteRequestEvent for disconnect: userId={}", userId);
        WatchingSessionDeleteRequestEvent deleteEvent = new WatchingSessionDeleteRequestEvent(null, userId);
        rabbitTemplate.convertAndSend(WS_EXCHANGE, WS_DELETE_ROUTING, deleteEvent);

        String profileImageUrl = imageQueryService.getPresignedUrl(imageKey);
        destinations.forEach(destination -> {
            UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
            UUID liveChatRoomId = contentId;

            SendPresenceMessageCommand sendPresenceMessageCommand =
                    new SendPresenceMessageCommand(
                            UUID.randomUUID(),
                            Instant.now(),
                            userId,
                            name,
                            profileImageUrl,
                            WatchType.LEAVE.name(),
                            destination
                    );
            log.info("Sending Presence Message (LEAVE) for disconnect: liveChatRoomId={}, userId={}", liveChatRoomId, userId);
            liveChatRoomCommandService.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
        });
    }

    private UUID getUserId(Principal principal) {
        return UUID.fromString(principal.getName());
    }

}
