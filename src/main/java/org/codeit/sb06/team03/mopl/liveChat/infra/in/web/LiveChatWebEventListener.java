package org.codeit.sb06.team03.mopl.liveChat.infra.in.web;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.liveChat.application.in.SendPresenceMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.command.SendPresenceMessageCommand;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.codeit.sb06.team03.mopl.watchingSession.WatchingSessionReadModel;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionCommand;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.CreateWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.DeleteWatchingSessionUseCase;
import org.codeit.sb06.team03.mopl.watchingSession.application.in.GetWatchingSessionUseCase;
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
public class LiveChatWebEventListener {

    private final CreateWatchingSessionUseCase createWatchingSessionUseCase;
    private final SendPresenceMessageUseCase sendPresenceMessageUseCase;
    private final DeleteWatchingSessionUseCase deleteWatchingSessionUseCase;
    private final GetWatchingSessionUseCase getWatchingSessionUseCase;

    // 같은 채널을 구독하지 못하게 하는 로직 필요
    @EventListener
    void onLiveChatSubscribedEvent(SessionSubscribeEvent event) {

        if (event.getUser() == null) return;

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = accessor.getDestination();
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) return;

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatId = contentId; // LiveChat은 Content와 같은 ID를 쓰고 있음.

        MoplUserDetails userDetails = getUserDetails(event.getUser());
        UserDto userDto = userDetails.getUserDto();

        CreateWatchingSessionCommand createWatchingSessionCommand = new CreateWatchingSessionCommand(liveChatId, userDto.id());
        createWatchingSessionUseCase.create(createWatchingSessionCommand);

        WatchingSessionReadModel watchingSession = getWatchingSessionUseCase.getByContentId(userDto.id());
        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        watchingSession.id(),
                        watchingSession.createdAt(),
                        userDto.id(),
                        userDto.name(),
                        userDto.profilePresignedUrl(),
                        WatchType.JOIN.name(),
                        destination
                );
        sendPresenceMessageUseCase.sendPresenceMessage(liveChatId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatUnSubscribedEvent(SessionUnsubscribeEvent event) {

        if (event.getUser() == null) return;

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) return;

        String destination = (String) accessor.getSessionAttributes().get(accessor.getSubscriptionId());
        if (destination == null || !DestinationUtils.matchWatchSubDestination(destination)) return;

        accessor.getSessionAttributes().remove(accessor.getSubscriptionId());

        MoplUserDetails userDetails = getUserDetails(event.getUser());
        UserDto userDto = userDetails.getUserDto();

        UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
        UUID liveChatId = contentId; // LiveChat은 Content와 같은 ID를 쓰고 있음.

        WatchingSessionReadModel watchingSession = getWatchingSessionUseCase.getByContentId(userDto.id());
        deleteWatchingSessionUseCase.delete(watchingSession.id());

        SendPresenceMessageCommand sendPresenceMessageCommand =
                new SendPresenceMessageCommand(
                        watchingSession.id(),
                        watchingSession.createdAt(),
                        userDto.id(),
                        userDto.name(),
                        userDto.profilePresignedUrl(),
                        WatchType.LEAVE.name(),
                        destination
                );

        sendPresenceMessageUseCase.sendPresenceMessage(liveChatId, sendPresenceMessageCommand);
    }

    @EventListener
    void onLiveChatDisconnectedEvent(SessionDisconnectEvent event) {

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
        } catch (org.codeit.sb06.team03.mopl.watchingSession.domain.exception.WatchingSessionNotFoundException e) {
            // 이미 삭제된 경우 예외 무시
        }

        deleteWatchingSessionUseCase.deleteByWatcherId(userDto.id());

        if (watchingSession != null) {
            WatchingSessionReadModel finalWatchingSession = watchingSession;
            destinations.forEach(destination -> {
                UUID contentId = UUID.fromString(DestinationUtils.extractContentId(destination));
                UUID liveChatId = contentId;

                SendPresenceMessageCommand sendPresenceMessageCommand =
                        new SendPresenceMessageCommand(
                                finalWatchingSession.id(),
                                finalWatchingSession.createdAt(),
                                userDto.id(),
                                userDto.name(),
                                userDto.profilePresignedUrl(),
                                WatchType.LEAVE.name(),
                                destination
                        );
                sendPresenceMessageUseCase.sendPresenceMessage(liveChatId, sendPresenceMessageCommand);
            });
        }
    }

    private MoplUserDetails getUserDetails(Principal principal) {
        Authentication authentication = (Authentication) principal;
        return (MoplUserDetails) authentication.getPrincipal();
    }

}
