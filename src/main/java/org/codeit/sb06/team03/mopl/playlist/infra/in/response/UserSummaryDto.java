package org.codeit.sb06.team03.mopl.playlist.infra.in.response;

import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.in.UserDto;

import java.util.UUID;

public record UserSummaryDto (
        UUID userId,
        String name,
        String profileImageUrl
) {
    public static UserSummaryDto from(Profile profile, GetPresignedUrlUseCase getPresignedUrlUseCase) {

        String profileImageUrl = getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey());

        return new UserSummaryDto(
                profile.getAccountId(),
                profile.getName(),
                profileImageUrl
        );
    }

    public static UserSummaryDto from(UserDto userDto) {
        return new UserSummaryDto(
                userDto.id(), userDto.name(), userDto.profilePresignedUrl()
        );
    }
}

