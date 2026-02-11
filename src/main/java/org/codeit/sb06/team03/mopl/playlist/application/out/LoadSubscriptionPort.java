package org.codeit.sb06.team03.mopl.playlist.application.out;

import org.codeit.sb06.team03.mopl.playlist.domain.entity.SubscriptionId;

public interface LoadSubscriptionPort {
    boolean existsById(SubscriptionId id);
}
