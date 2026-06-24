package org.codeit.sb06.team03.mopl.dm.conversation.infra.out;

import org.codeit.sb06.team03.mopl.dm.conversation.application.out.LoadDMUserPort;
import org.codeit.sb06.team03.mopl.dm.conversation.domain.vo.DMUser;
import org.codeit.sb06.team03.mopl.image.application.in.GetPresignedUrlUseCase;
import org.codeit.sb06.team03.mopl.profile.application.in.GetProfileUseCase;
import org.codeit.sb06.team03.mopl.profile.domain.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoadDMUserAdapter implements LoadDMUserPort {

    private final GetProfileUseCase getProfileUseCase;
    private final GetPresignedUrlUseCase getPresignedUrlUseCase;

    public LoadDMUserAdapter(GetProfileUseCase getProfileUseCase, GetPresignedUrlUseCase getPresignedUrlUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.getPresignedUrlUseCase = getPresignedUrlUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    public DMUser findByUserId(UUID userId) {
        Profile profile = getProfileUseCase.getDMUserProfile(userId).orElseThrow();
        String url = getPresignedUrlUseCase.getPresignedUrl(profile.getImageKey());
        return new DMUser(userId, profile.getName(), url);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, DMUser> findByUserIds(Set<UUID> userIds) {
        return userIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        this::findByUserId
                ));
    }
}

