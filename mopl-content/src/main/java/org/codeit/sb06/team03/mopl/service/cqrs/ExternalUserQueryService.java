package org.codeit.sb06.team03.mopl.service.cqrs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.client.UserClient;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(value = "contentTransactionManager")
public class ExternalUserQueryService {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final UserClient userClient;

    public ExternalUserView getProfile(UUID userId) {
        if (userId == null) {
            return null;
        }
        return externalUserViewRepository.findById(userId)
                .orElseGet(() -> fetchAndSaveProfile(userId));
    }

    private ExternalUserView fetchAndSaveProfile(UUID userId) {
        UserClient.UserDto userDto = userClient.getUserById(userId);
        if (userDto == null) {
            log.warn("Failed to fetch user info via REST for userId: {}", userId);
            return null;
        }
        ExternalUserView view = ExternalUserView.create(userDto.id(), userDto.name(), userDto.profileImageUrl());
        try {
            return externalUserViewRepository.save(view);
        } catch (Exception e) {
            log.error("Failed to save replicated ExternalUserView for userId: {}", userId, e);
            return view;
        }
    }
}
