package org.codeit.sb06.team03.mopl.liveChatRoom.infra.in.web;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.SendPresenceMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChatRoom.application.in.command.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.codeit.sb06.team03.mopl.cache.ProfileImageCache;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionCommand;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.DeleteWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionNotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LiveChatRoomWebEventListener {

    private final CreateWatchingSessionUseCase createWatchingSessionUseCase;
    private final SendPresenceMessageUseCase sendPresenceMessageUseCase;
    private final DeleteWatchingSessionUseCase deleteWatchingSessionUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;
    private final ProfileImageCache profileImageCache;

    // 같은 채널을 구독하지 못하게 하는 로직 필요
    @EventListener
    void onLiveChatRoomSubscribedEvent(SessionSubscribeEvent event) {

        if (event.getUser() == null) return;

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = accessor.getDestination();
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) return;

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatRoomId = contentId; // LiveChatRoom은 Content와 같은 ID를 쓰고 있음.

        MoplUserDetails userDetails = getUserDetails(event.getUser());
        UserDto userDto = userDetails.getUserDto();

        CreateWatchingSessionCommand createWatchingSessionCommand = new CreateWatchingSessionCommand(liveChatRoomId, userDto.id());
        createWatchingSessionUseCase.create(createWatchingSessionCommand);

        WatchingSessionReadModel watchingSession = getWatchingSessionUseCase.getByContentId(userDto.id());
        String profileImageUrl = profileImageCache.getProfileImageUrl(userDto.id());
        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        watchingSession.id(),
                        watchingSession.createdAt(),
                        userDto.id(),
                        userDto.name(),
                        profileImageUrl,
                        WatchType.JOIN.name(),
                        destination
                );
        sendPresenceMessageUseCase.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatRoomUnSubscribedEvent(SessionUnsubscribeEvent event) {

        if (event.getUser() == null) return;

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = (String) accessor.getSessionAttributes().get(accessor.getSubscriptionId());
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) return;

        accessor.getSessionAttributes().remove(accessor.getSubscriptionId());

        MoplUserDetails userDetails = getUserDetails(event.getUser());
        UserDto userDto = userDetails.getUserDto();

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatRoomId = contentId; // LiveChatRoom은 Content와 같은 ID를 쓰고 있음.

        WatchingSessionReadModel watchingSession = getWatchingSessionUseCase.getByContentId(userDto.id());
        if (watchingSession == null) return;

        deleteWatchingSessionUseCase.delete(watchingSession.id());

        String profileImageUrl = profileImageCache.getProfileImageUrl(userDto.id());
        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        watchingSession.id(),
                        watchingSession.createdAt(),
                        userDto.id(),
                        userDto.name(),
                        profileImageUrl,
                        WatchType.LEAVE.name(),
                        destination
                );

        sendPresenceMessageUseCase.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatRoomDisconnectedEvent(SessionDisconnectEvent event) {

        if (event.getUser() == null) return; // user가 null인 경우는 connect에서 setUser를 하기 전에 종료되었을 때 뿐임, 그러므로 데이터베이스 작업은 불필요함

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;
        if (accessor.getSessionAttributes() == null || accessor.getSessionAttributes().isEmpty()) return;

        MoplUserDetails userDetails = getUserDetails(event.getUser());
        UserDto userDto = userDetails.getUserDto();

        List<String> destinations = accessor.getSessionAttributes().values()
                .stream().map(value -> (String) value)
                .filter(DestinationUtils::matchWatchSubDestination)
                .toList();

        if (destinations.isEmpty()) return;

        WatchingSessionReadModel watchingSession = null;
        try {
            watchingSession = getWatchingSessionUseCase.getByContentId(userDto.id());
        } catch (WatchingSessionNotFoundException e) {
            // 이미 삭제된 경우 예외 무시
        }

        deleteWatchingSessionUseCase.deleteByWatcherId(userDto.id());

        if (watchingSession != null) {
            WatchingSessionReadModel finalWatchingSession = watchingSession;
            String profileImageUrl = profileImageCache.getProfileImageUrl(userDto.id());
            destinations.forEach(destination -> {
                UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
                UUID liveChatRoomId = contentId;

                SendPresenceMessageCommand sendPresenceMessageCommand =
                        new SendPresenceMessageCommand(
                                finalWatchingSession.id(),
                                finalWatchingSession.createdAt(),
                                userDto.id(),
                                userDto.name(),
                                profileImageUrl,
                                WatchType.LEAVE.name(),
                                destination
                        );
                sendPresenceMessageUseCase.sendPresenceMessage(liveChatRoomId, sendPresenceMessageCommand);
            });
        }
    }

    private MoplUserDetails getUserDetails(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return (MoplUserDetails) authentication.getPrincipal();
    }
}
