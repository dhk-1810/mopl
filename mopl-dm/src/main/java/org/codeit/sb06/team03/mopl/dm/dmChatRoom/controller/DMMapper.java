package org.codeit.sb06.team03.mopl.dm.dmChatRoom.controller;

import org.codeit.sb06.team03.mopl.dm.dmChatRoom.service.CreateDMChatRoomCommand;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DMMapper {

    public CreateDMChatRoomCommand toCommand(UUID request) {
        return new CreateDMChatRoomCommand(request);
    }
}