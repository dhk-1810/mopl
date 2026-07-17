package org.codeit.sb06.team03.mopl.dm.dmChatRoom.exception;

import java.util.UUID;

public class DMChatRoomAlreadyExistsException extends DMException{

    public DMChatRoomAlreadyExistsException(UUID withUserId) {
        super("Already Exists : '%s'".formatted(withUserId.toString()));
    }
}
