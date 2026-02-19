package org.codeit.sb06.team03.mopl.playlist.application.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LoadSubscriptionPort {

    boolean existsById(SubscriptionId id);

    Map<UUID, Boolean> existsByIdIn(List<UUID> ids, UUID userId);

    Optional<Subscription> findById(SubscriptionId id);

}
