package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

public record FollowedEvent(
        @JsonAlias({"followeeId", "userId"}) UUID userId,
        UUID followerId
) {}
