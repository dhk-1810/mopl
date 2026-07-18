package org.codeit.sb06.team03.mopl.exception;

import java.util.UUID;

public class DMMessageNotFoundException extends DMException{
    public DMMessageNotFoundException(UUID messageId) {
        super("Not Found : '%s'".formatted(messageId.toString()));
    }
}
