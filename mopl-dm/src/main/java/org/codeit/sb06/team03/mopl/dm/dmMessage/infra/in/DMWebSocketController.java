package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.DMCompositeService;
import org.codeit.sb06.team03.mopl.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in.request.MessageSendRequest;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DMWebSocketController {

    private final DMCompositeService dmCompositeService;

    @MessageMapping("/conversations/{conversationId}/direct-messages")
    public void sendMessage(
            @DestinationVariable UUID conversationId,
            @Payload MessageSendRequest request,
            Principal principal
    ) {
        log.info("DMWebSocketController.sendMessage called: conversationId={}, payload={}", conversationId, request);
        Authentication authentication = (Authentication) principal;
        MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
        UserDto userDto = userDetails.getUserDto();

        dmCompositeService.sendDM(conversationId, userDto.id(), request);
    }
}
