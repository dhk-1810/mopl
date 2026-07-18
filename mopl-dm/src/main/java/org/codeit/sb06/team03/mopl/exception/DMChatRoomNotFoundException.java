package org.codeit.sb06.team03.mopl.exception;

import java.util.UUID;

public class DMChatRoomNotFoundException extends DMException{

    public DMChatRoomNotFoundException(UUID withUserId) {
        super("Not Found : '%s'".formatted(withUserId.toString()));
    }
}
