package org.codeit.sb06.team03.mopl.exception;

import java.util.UUID;

public class UserNotFoundException extends PlaylistException {
    public UserNotFoundException(UUID id) {
        super("User not found: %s".formatted(id));
    }
}
