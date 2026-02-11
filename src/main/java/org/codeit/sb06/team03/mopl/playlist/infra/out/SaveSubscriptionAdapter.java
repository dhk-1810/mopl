package org.codeit.sb06.team03.mopl.playlist.infra.out;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.playlist.application.out.SaveSubscriptionPort;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SaveSubscriptionAdapter implements SaveSubscriptionPort {

    private final SubscriptionRepository repository;

    @Override
    public void save(Subscription subscription) {
        repository.save(subscription);
    }

    @Override
    public void delete(SubscriptionId id) {
        repository.delete(id);
    }
}
