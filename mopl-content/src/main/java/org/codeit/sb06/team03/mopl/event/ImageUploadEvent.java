package org.codeit.sb06.team03.mopl.event;

import java.io.Serializable;

public record ImageUploadEvent(
        String key,
        String contentType
) implements Serializable {
}
