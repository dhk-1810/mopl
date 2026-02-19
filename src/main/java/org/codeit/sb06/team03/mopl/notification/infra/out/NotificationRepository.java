package org.codeit.sb06.team03.mopl.notification.infra.out;

import io.github.openfeign.querydsl.jpa.spring.repository.QuerydslJpaRepository;
import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.springframework.data.domain.Slice;

import java.util.UUID;

public interface NotificationRepository extends QuerydslJpaRepository<Notification, UUID> {

    default Slice<Notification> findAll(CursorGetNotificationsCondition condition) {

    }
}
