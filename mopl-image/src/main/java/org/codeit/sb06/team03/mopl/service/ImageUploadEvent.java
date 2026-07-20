package org.codeit.sb06.team03.mopl.service;

import java.io.Serializable;

public record ImageUploadEvent(
        String key,
        String contentType
) implements Serializable {
}
