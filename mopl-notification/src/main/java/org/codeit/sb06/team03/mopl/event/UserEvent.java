package org.codeit.sb06.team03.mopl.event;

import java.util.UUID;

public final class UserEvent {

    private UserEvent() {}

    public interface UserEventInterface {
        UUID userId();
    }

    public record UserProfileCreatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) implements UserEventInterface {}

    public record UserProfileUpdatedEvent(
            UUID userId,
            String name,
            String imageKey
    ) implements UserEventInterface {}

    public record RoleUpdatedEvent(
            UUID userId,
            String role
    ) implements UserEventInterface {}

    public record FollowedEvent(
            UUID userId,
            UUID followerId
    ) implements UserEventInterface {}
}
