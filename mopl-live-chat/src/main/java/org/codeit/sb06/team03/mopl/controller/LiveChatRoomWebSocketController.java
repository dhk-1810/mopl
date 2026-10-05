package org.codeit.sb06.team03.mopl.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dto.request.LiveChatRoomSendRequest;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.codeit.sb06.team03.mopl.profile.domain.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendLiveChatRoomMessageCommand;
import org.codeit.sb06.team03.mopl.util.DestinationUtils;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveChatRoomWebSocketController {

    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ProfileQueryService profileQueryService;
    private final ExternalImageQueryService imageQueryService;

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
        ProfileReadModel profile = profileQueryService.getProfileReadModel(userId);
        String name = profile != null ? profile.name() : "Unknown User";
        String imageKey = profile != null ? profile.imageKey() : null;

        String destination = DestinationUtils.liveChatRoomSendResponseDestinationFormat.formatted(contentId);
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
