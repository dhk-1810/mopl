package org.codeit.sb06.team03.mopl.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

public class UserEvent {

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

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class RoleUpdatedEvent extends UserEvent {
        private UUID accountId;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class FollowedEvent extends UserEvent {
        private UUID followeeId;
        private UUID followerId;
    }
}
