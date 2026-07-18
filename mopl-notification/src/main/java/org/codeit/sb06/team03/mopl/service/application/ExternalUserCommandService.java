package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "notificationTransactionManager")
public class ExternalUserCommandService {

    private final ExternalUserViewRepository externalUserViewRepository;

    public void createOrUpdateProfile(UUID userId, String name, String imageKey) {
        ExternalUserView userView = externalUserViewRepository.findById(userId)
                .orElseGet(() -> ExternalUserView.create(userId, name, imageKey));
        userView.update(name, imageKey);
        externalUserViewRepository.save(userView);
    }
}
