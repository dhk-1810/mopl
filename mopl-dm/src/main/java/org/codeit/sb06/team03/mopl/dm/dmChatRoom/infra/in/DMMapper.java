package org.codeit.sb06.team03.mopl.dm.dmChatRoom.infra.in;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.in.CreateDMChatRoomCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DMMapper {

    public CreateDMChatRoomCommand toCommand(UUID request) {
        return new CreateDMChatRoomCommand(request);
    }
}