package org.codeit.sb06.team03.mopl.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.notification.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.notification.domain.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.notification.repository.ExternalUserViewRepository;
import org.codeit.sb06.team03.mopl.profile.domain.event.UserEvent.UserProfileCreatedEvent;
import org.codeit.sb06.team03.mopl.profile.domain.event.UserEvent.UserProfileUpdatedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileEventListener {

    private final ExternalUserViewRepository externalUserViewRepository;

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    @Transactional(value = "notificationTransactionManager")
    public void handleProfileCreated(UserProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-notification: {}", event);
        ExternalUserView userView = ExternalUserView.create(event.userId(), event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    @Transactional(value = "notificationTransactionManager")
    public void handleProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ in mopl-notification: {}", event);
        ExternalUserView userView = externalUserViewRepository.findById(event.userId())
                .orElseGet(() -> ExternalUserView.create(event.userId(), event.name(), event.imageKey()));
        userView.update(event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }
}
