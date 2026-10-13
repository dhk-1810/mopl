package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record FollowKafkaEvent(
        String eventType,
        UUID followeeId,
        UUID followerId
) {
}
