package org.codeit.sb06.team03.mopl.notification.infra.out;

import org.codeit.sb06.team03.mopl.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
