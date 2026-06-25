package org.codeit.sb06.team03.mopl.profile;

import org.codeit.sb06.team03.mopl.profile.domain.Profile;

import java.util.UUID;

public record ProfileReadModel(
        UUID userId,
        String name,
        String imageKey,
        String email,
        String role
) {
    public static ProfileReadModel from(Profile profile) {
        String email = profile.getAccount() != null && profile.getAccount().getEmailAddress() != null ?
                profile.getAccount().getEmailAddress().value() : null;
        String role = profile.getAccount() != null && profile.getAccount().getRole() != null ?
                profile.getAccount().getRole().name() : null;

        return new ProfileReadModel(
                profile.getAccountId(),
                profile.getName(),
                profile.getImageKey(),
                email,
                role
        );
    }
}
