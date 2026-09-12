package org.codeit.sb06.team03.mopl.event;

import org.codeit.sb06.team03.mopl.entity.vo.Role;
import java.util.UUID;

public record RoleUpdatedEvent(
        UUID accountId,
        Role role
) {}
