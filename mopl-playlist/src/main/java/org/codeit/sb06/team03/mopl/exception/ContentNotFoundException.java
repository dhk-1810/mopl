package org.codeit.sb06.team03.mopl.exception;

import java.util.UUID;

public class ContentNotFoundException extends PlaylistException {
    public ContentNotFoundException(UUID id) {
        super("Content not found: %s".formatted(id));
    }
}
