package org.codeit.sb06.team03.mopl.profile.service;

import java.util.UUID;

public record CreateProfileCommand(UUID accountId, String name) {
}
