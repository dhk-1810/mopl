package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.DestinationUtils;
import org.codeit.sb06.team03.mopl.config.UserIdHandshakeInterceptor;
import org.codeit.sb06.team03.mopl.dto.request.LiveChatRoomSendRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendLiveChatRoomMessageCommand;
import org.codeit.sb06.team03.mopl.service.ImageQueryService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveChatRoomWebSocketController implements LiveChatRoomApi {

    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ProfileQueryService profileQueryService;
    private final ImageQueryService imageQueryService;

    @Override
    @MessageMapping("/contents/{contentId}/chat")
    public void pubMessage(
            @DestinationVariable String contentId,
            @Payload LiveChatRoomSendRequest request,
            Principal principal
    ) {
        String userIdStr = principal != null ? principal.getName() : null;

        if (userIdStr == null || userIdStr.isBlank()) {
            log.warn("Cannot send live chat message: missing user identity for contentId={}", contentId);
            return;
        }

        UUID userId = UUID.fromString(userIdStr);
        String name = "Unknown User";
        String imageKey = null;
        try {
            Profile profile = profileQueryService.getById(userId);
            if (profile != null) {
                name = profile.getName();
                imageKey = profile.getImageKey();
            }
        } catch (Exception ignored) {
        }

        String destination = DestinationUtils.liveChatRoomSendResponseDestinationFormat.formatted(contentId);

        /* 실시간 채팅 프로필 사진 응답
         */
        String profileImageUrl = imageQueryService.getPresignedUrl(imageKey);

        SendLiveChatRoomMessageCommand command = new SendLiveChatRoomMessageCommand(
                userId,
                name,
                profileImageUrl,
                request.text(),
                destination
            );
        liveChatRoomCommandService.sendLiveChatRoomMessage(command);
    }
}
