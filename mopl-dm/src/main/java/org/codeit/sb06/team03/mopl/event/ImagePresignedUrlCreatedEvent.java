package org.codeit.sb06.team03.mopl.event;

import java.io.Serializable;
import java.time.Instant;

public record ImagePresignedUrlCreatedEvent(
        String key,
        String presignedUrl,
        Instant exp
) implements Serializable {
}
