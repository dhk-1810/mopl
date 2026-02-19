package org.codeit.sb06.team03.mopl.playlist.infra.out;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.QSubscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.Subscription;
import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public interface SubscriptionRepository extends QuerydslJpaRepository<Subscription, SubscriptionId> {

    boolean existsById(SubscriptionId id);

    Optional<Subscription> findById(SubscriptionId id);

    void deleteById(SubscriptionId id);

    default Map<UUID, Boolean> findAllSubscribedMap(List<UUID> playlistIds, UUID viewerId) {
        QSubscription subscription = QSubscription.subscription;

        List<UUID> subscribedIds =
                select(subscription.id.playlistId)
                .from(subscription)
                .where(subscription.id.playlistId.in(playlistIds)
                        .and(subscription.id.subscriberId.eq(viewerId)))
                .fetch();

        return playlistIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        subscribedIds::contains
                ));
    }

    default void deleteAllByPlaylistId(UUID playlistId) {
        QSubscription subscription = QSubscription.subscription;
        delete(subscription)
                .where(subscription.id.playlistId.eq(playlistId))
                .execute();
    }

    default void deleteAllBySubscriberId(UUID subscriberId) {
        QSubscription subscription = QSubscription.subscription;
        delete(subscription)
                .where(subscription.id.subscriberId.eq(subscriberId))
                .execute();
    }


}
