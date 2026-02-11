package org.codeit.sb06.team03.mopl.playlist.domain;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SubscriptionService {

    public Subscription create(UUID playlistId, UUID userId) {
        return Subscription.create(playlistId, userId);
    }
}
