package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.notification.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.notification.repository.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "notificationTransactionManager", readOnly = true)
public class ExternalUserQueryService {

    private final ExternalUserViewRepository externalUserViewRepository;

    public ExternalUserView getProfile(UUID userId) {
        return externalUserViewRepository.findById(userId)
                .orElse(null);
    }
}
