package org.codeit.sb06.team03.mopl.profile.application.in;

import java.util.UUID;

public record CreateProfileCommand(UUID accountId, String name) {
}
