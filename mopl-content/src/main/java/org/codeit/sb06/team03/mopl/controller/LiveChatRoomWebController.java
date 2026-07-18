package org.codeit.sb06.team03.mopl.controller;
import org.codeit.sb06.team03.mopl.DestinationUtils;
import org.codeit.sb06.team03.mopl.dto.request.LiveChatRoomSendRequest;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.service.application.LiveChatRoomCommandService;
import org.codeit.sb06.team03.mopl.service.application.SendLiveChatRoomMessageCommand;
import org.codeit.sb06.team03.mopl.profile.controller.UserDto;
import org.codeit.sb06.team03.mopl.cache.ProfileImageCache;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LiveChatRoomWebController implements LiveChatRoomApi {

    private final LiveChatRoomCommandService liveChatRoomCommandService;
    private final ProfileImageCache profileImageCache;

    @MessageMapping("/contents/{contentId}/chat")
    public void pubMessage(
            @DestinationVariable String contentId,
            @Payload LiveChatRoomSendRequest request,
            Principal principal
    ) {
        Authentication authentication = (Authentication) principal;
        MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
        UserDto userDto = userDetails.getUserDto();

        String destination = DestinationUtils.liveChatRoomSendResponseDestinationFormat.formatted(contentId);

        /* 실시간 채팅 프로필 사진 응답
         */
        String profileImageUrl = profileImageCache.getProfileImageUrl(userDto.id());

        SendLiveChatRoomMessageCommand command = new SendLiveChatRoomMessageCommand(
                userDto.id(),
                userDto.name(),
                profileImageUrl,
                request.text(),
                destination
            );
        liveChatRoomCommandService.sendLiveChatRoomMessage(command);
    }
}
