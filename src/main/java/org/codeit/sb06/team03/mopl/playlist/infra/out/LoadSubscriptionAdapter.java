package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.LoadSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LoadSubscriptionAdapter implements LoadSubscriptionPort {

    private final SubscriptionRepository subscriptionRepository;

    @Override
    public boolean existsById(SubscriptionId id) {
        return subscriptionRepository.existsById(id);
    }
}
