package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.DMCompositeService;
import org.codeit.sb06.team03.mopl.common.security.MoplUserDetails;
import org.codeit.sb06.team03.mopl.dm.dmMessage.infra.in.request.MessageSendRequest;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DMWebSocketController {

    private final DMCompositeService dmCompositeService;

    @MessageMapping("/dm_chat_rooms/{dmChatRoomId}/direct-messages")
    public void sendMessage(
            @DestinationVariable UUID dmChatRoomId,
            @Payload MessageSendRequest request,
            Principal principal
    ) {
        Authentication authentication = (Authentication) principal;
        MoplUserDetails userDetails = (MoplUserDetails) authentication.getPrincipal();
        UserDto userDto = userDetails.getUserDto();

        dmCompositeService.sendDM(dmChatRoomId, userDto.id(), request);
    }
}