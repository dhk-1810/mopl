package org.codeit.sb06.team03.mopl.event;

public record ImageUploadEvent(
        String key,
        String contentType
) {
}
