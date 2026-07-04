package org.codeit.sb06.team03.mopl.profile.infra.in;

import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        Instant createdAt,
        String email,
        String name,
        String profileImageUrl,
        String role,
        Boolean locked
) {
    public static UserDto from(Account account, Profile profile, String profileImageUrl) {
        return new UserDto(
                account.getId(),
                account.getCreatedAt(),
                account.getEmailAddress().value(),
                profile.getName(),
                profileImageUrl,
                account.getRole().name(),
                account.isLocked()
        );
    }
}
