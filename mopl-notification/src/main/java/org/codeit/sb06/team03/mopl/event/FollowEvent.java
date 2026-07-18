package org.codeit.sb06.team03.mopl.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

public abstract class FollowEvent {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class FollowedEvent extends FollowEvent {
        private UUID followeeId;
        private UUID followerId;
    }
}
