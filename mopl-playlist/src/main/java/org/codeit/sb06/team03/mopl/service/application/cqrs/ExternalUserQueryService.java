package org.codeit.sb06.team03.mopl.service.application.cqrs;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.infra.out.cqrs.ExternalUserViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class ExternalUserQueryService {

    private final ExternalUserViewRepository externalUserViewRepository;

    public Map<UUID, ExternalUserView> getProfiles(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return externalUserViewRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(ExternalUserView::getId, Function.identity()));
    }

    public ExternalUserView getProfile(UUID userId) {
        return externalUserViewRepository.findById(userId)
                .orElse(null);
    }
}
