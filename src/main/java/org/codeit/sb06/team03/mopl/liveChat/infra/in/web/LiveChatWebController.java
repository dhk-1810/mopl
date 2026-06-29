package org.codeit.sb06.team03.mopl.liveChat.infra.in.web;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.liveChat.application.in.SendLiveChatMessageUseCase;
import org.codeit.sb06.team03.mopl.liveChat.application.in.command.SendLiveChatMessageCommand;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.codeit.sb06.team03.mopl.common.cache.ProfileImageCache;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LiveChatWebController implements LiveChatApi {

    private final SendLiveChatMessageUseCase sendLiveChatMessageUseCase;
    private final ProfileImageCache profileImageCache;

    @MessageMapping("/contents/{contentId}/chat")
    public void pubMessage(
            @DestinationVariable String contentId,
            @Payload LiveChatSendRequest request,
            Principal principal
    ) {
        Authentication authentication = (Authentication) principal;
        MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
        UserDto userDto = userDetails.getUserDto();

        String destination = DestinationUtils.liveChatSendResponseDestinationFormat.formatted(contentId);

        /* 실시간 채팅 프로필 사진 응답
        1. 매 채팅마다 DB에서 presigned URL 조회 -> 부하 너무 큼.
        2. MoplUserDetails 활용, UserDto의 profileImageUrl(presigned URL) 전송.
          가장 간단하고 적절함. 프로필사진 등록/변경/삭제 또는 presigned URL 만료 시 lazy하게 URL 업데이트하면 됨.
          그러나 URL의 TTL 만료 시 업데이트가 정상 작동하려면 프론트에서 소켓을 재호출해야 함. 본 프로젝트는 프론트 코드 수정은 불가.
        3. Redis (채택)
          presigned URL redis에 캐싱, URL 만료되면 Lazy Load, 프로필 등록/변경/삭제시 교체/삭제
          캐시 만료시 Lazy Load(Cache Aside).
         */
        String profileImageUrl = profileImageCache.getProfileImageUrl(userDto.id());

        SendLiveChatMessageCommand command = new SendLiveChatMessageCommand(
                userDto.id(),
                userDto.name(),
                profileImageUrl,
                request.text(),
                destination
        );
        sendLiveChatMessageUseCase.sendLiveChatMessage(command);
    }
}
