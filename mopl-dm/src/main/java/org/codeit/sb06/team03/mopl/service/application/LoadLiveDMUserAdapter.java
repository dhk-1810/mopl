package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.service.ProfileQueryService;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.service.ImageQueryService;
import org.codeit.sb06.team03.mopl.dto.UserSummary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadLiveDMUserAdapter {

    private final ProfileQueryService profileQueryService;
    private final ImageQueryService imageQueryService;

    public UserSummary findByUserId(UUID userId) {
        String name = "Unknown User";
        String imageKey = null;
        try {
            Profile profile = profileQueryService.getById(userId);
            if (profile != null) {
                name = profile.getName();
                imageKey = profile.getImageKey();
            }
        } catch (Exception ignored) {
        }
        String url = imageQueryService.getPresignedUrl(imageKey);
        return new UserSummary(userId, name, url);
    }
}
