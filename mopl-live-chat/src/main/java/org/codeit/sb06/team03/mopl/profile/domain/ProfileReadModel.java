package org.codeit.sb06.team03.mopl.profile.domain;

import org.codeit.sb06.team03.mopl.profile.domain.entity.ExternalProfileView;
import java.util.UUID;

public record ProfileReadModel(
        UUID userId,
        String name,
        String imageKey
) {
    public static ProfileReadModel from(ExternalProfileView profile) {
        if (profile == null) return null;
        return new ProfileReadModel(
                profile.getAccountId(),
                profile.getName(),
                profile.getImageKey()
        );
    }
}
