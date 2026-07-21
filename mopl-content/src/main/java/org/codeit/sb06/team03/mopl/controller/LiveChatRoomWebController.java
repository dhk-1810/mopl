package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.DestinationUtils;
import org.codeit.sb06.team03.mopl.dto.request.LiveChatRoomSendRequest;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendLiveChatRoomMessageCommand;
import org.codeit.sb06.team03.mopl.image.service.ExternalImageQueryService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class LiveChatRoomWebController implements LiveChatRoomApi {

    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ExternalUserQueryService externalUserQueryService;
    private final ExternalImageQueryService imageQueryService;

    @MessageMapping("/contents/{contentId}/chat")
    public void pubMessage(
            @DestinationVariable String contentId,
            @Payload LiveChatRoomSendRequest request,
            Principal principal
    ) {
        UUID userId = UUID.fromString(principal.getName());
        ExternalUserView userView = externalUserQueryService.getProfile(userId);
        String name = userView != null ? userView.getName() : "Unknown User";
        String imageKey = userView != null ? userView.getProfileImageKey() : null;

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
