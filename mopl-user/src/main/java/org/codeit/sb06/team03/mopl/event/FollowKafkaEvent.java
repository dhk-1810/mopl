package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public record FollowKafkaEvent(
        String eventType,
        UUID followeeId,
        UUID followerId
) {
    public static FollowKafkaEvent followed(UUID followeeId, UUID followerId) {
        return new FollowKafkaEvent("FOLLOWED", followeeId, followerId);
    }

    public static FollowKafkaEvent unfollowed(UUID followeeId, UUID followerId) {
        return new FollowKafkaEvent("UNFOLLOWED", followeeId, followerId);
    }
}
