package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

public record RoleUpdatedEvent(
        @JsonAlias({"accountId", "userId"}) UUID userId,
        String role
) {}
