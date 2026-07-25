package org.codeit.sb06.team03.mopl.dto.response;

import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.entity.Profile;

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
