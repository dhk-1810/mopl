package org.codeit.sb06.team03.mopl.event;

import java.time.Instant;

public record ImagePresignedUrlKafkaEvent(
        String key,
        String presignedUrl,
        Instant exp
) {
}
