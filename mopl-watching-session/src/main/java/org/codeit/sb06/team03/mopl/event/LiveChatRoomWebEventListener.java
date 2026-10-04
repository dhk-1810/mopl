package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.enums.WatchType;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.service.application.WatchingSessionCommandService;
import org.codeit.sb06.team03.mopl.util.DestinationUtils;
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
    private final WatchingSessionCommandService watchingSessionCommandService;
    private final ProfileQueryService profileQueryService;
    private final ExternalImageQueryService imageQueryService;

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
        UUID liveChatRoomId = contentId;

        UUID userId = getUserId(event.getUser());
        ProfileReadModel profile = profileQueryService.getProfileReadModel(userId);
        String name = profile != null ? profile.name() : "Unknown User";
        String imageKey = profile != null ? profile.imageKey() : null;

        UUID sessionId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        // 1. 같은 서비스 내에서 WatchingSession 직접 생성 (Redis 저장)
        try {
            watchingSessionCommandService.createWithId(sessionId, liveChatRoomId, userId, createdAt);
            log.info("Successfully created watching session directly: sessionId={}, liveChatRoomId={}, userId={}", sessionId, liveChatRoomId, userId);
        } catch (Exception e) {
            log.warn("Failed or duplicate watching session: sessionId={}, liveChatRoomId={}, userId={}, error={}", sessionId, liveChatRoomId, userId, e.getMessage());
        }

        // 2. Presence 메시지 브로드캐스팅
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
        ProfileReadModel profile = profileQueryService.getProfileReadModel(userId);
        String name = profile != null ? profile.name() : "Unknown User";
        String imageKey = profile != null ? profile.imageKey() : null;

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatRoomId = contentId;

        // 1. 같은 서비스 내에서 WatchingSession 직접 삭제
        try {
            watchingSessionCommandService.deleteByWatcherId(userId);
            log.info("Successfully deleted watching session directly: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to delete watching session: userId={}, error={}", userId, e.getMessage());
        }

        // 2. Presence 메시지 브로드캐스팅 (LEAVE)
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
            return;
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
        ProfileReadModel profile = profileQueryService.getProfileReadModel(userId);
        String name = profile != null ? profile.name() : "Unknown User";
        String imageKey = profile != null ? profile.imageKey() : null;

        List<String> destinations = accessor.getSessionAttributes().values()
                .stream().map(value -> (String) value)
                .filter(DestinationUtils::matchWatchSubDestination)
                .toList();

        log.info("onLiveChatRoomDisconnectedEvent active destinations: {}", destinations);
        if (destinations.isEmpty()) return;

        // 1. WatchingSession 직접 삭제
        try {
            watchingSessionCommandService.deleteByWatcherId(userId);
            log.info("Successfully deleted watching session directly on disconnect: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to delete watching session on disconnect: userId={}, error={}", userId, e.getMessage());
        }

        // 2. Presence 메시지 브로드캐스팅 (LEAVE)
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
