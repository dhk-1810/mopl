package org.codeit.sb06.team03.mopl.playlist.infra.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsById(SubscriptionId id);

    Optional<Subscription> findById(SubscriptionId id);

    void deleteById(SubscriptionId id);
}
