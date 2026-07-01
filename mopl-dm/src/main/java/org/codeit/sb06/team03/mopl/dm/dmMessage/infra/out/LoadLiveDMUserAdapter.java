package org.codeit.sb06.team03.mopl.dm.dmMessage.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.dm.dmMessage.application.out.LoadLiveDMUserPort;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.UserSummary;
import org.codeit.sb06.team03.mopl.profile.ProfileReadModel;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LoadLiveDMUserAdapter implements LoadLiveDMUserPort {

    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    @Override
    public UserSummary findByUserId(UUID userId) {
        ProfileReadModel profile = getProfileUseCase.getProfileReadModel(userId);
        String url = getPresignedUrlUseCase.getPresignedUrl(profile.imageKey());
        return new UserSummary(profile.userId(), profile.name(), url);
    }
}


