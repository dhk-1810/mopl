package org.codeit.sb06.team03.mopl.service.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.domain.entity.SubscriptionId;
import org.codeit.sb06.team03.mopl.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional(value = "playlistTransactionManager", readOnly = true)
public class SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    public boolean isSubscribed(UUID playlistId, UUID viewerId) {
        SubscriptionId id = new SubscriptionId(playlistId, viewerId);
        return subscriptionRepository.existsById(id);
    }

    public Map<UUID, Boolean> isSubscribed(Set<UUID> playlistIds, UUID viewerId) {
        return subscriptionRepository.findAllSubscribedMap(playlistIds, viewerId);
    }
}
