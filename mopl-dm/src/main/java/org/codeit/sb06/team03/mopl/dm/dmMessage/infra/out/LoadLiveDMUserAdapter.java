package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.application.ExternalUserQueryService;
import org.codeit.sb06.team03.mopl.dm.dmChatRoom.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadLiveDMUserPort;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadLiveDMUserAdapter implements LoadLiveDMUserPort {

    private final ExternalUserQueryService externalUserQueryService;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    @Override
    public UserSummary findByUserId(UUID userId) {
        ExternalUserView profile = externalUserQueryService.getProfile(userId);
        String name = "Unknown User";
        String imageKey = null;
        if (profile != null) {
            name = profile.getName();
            imageKey = profile.getProfileImageKey();
        }
        String url = getPresignedUrlUseCase.getPresignedUrl(imageKey);
        return new UserSummary(userId, name, url);
    }
}
