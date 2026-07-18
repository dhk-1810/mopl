package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.image.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadLiveDMUserAdapter {

    private final ExternalUserQueryService externalUserQueryService;
    private final ImageQueryService imageQueryService;

    public UserSummary findByUserId(UUID userId) {
        ExternalUserView profile = externalUserQueryService.getProfile(userId);
        String name = "Unknown User";
        String imageKey = null;
        if (profile != null) {
            name = profile.getName();
            imageKey = profile.getProfileImageKey();
        }
        String url = imageQueryService.getPresignedUrl(imageKey);
        return new UserSummary(userId, name, url);
    }
}
