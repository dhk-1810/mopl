package org.codeit.sb06.team03.mopl.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalUserViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileEventListener {

    private final ExternalUserViewRepository externalUserViewRepository;

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    @Transactional(value = "dmTransactionManager")
    public void handleProfileCreated(UserEvent.UserProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-dm: {}", event);
        ExternalUserView userView = ExternalUserView.create(event.userId(), event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    @Transactional(value = "dmTransactionManager")
    public void handleProfileUpdated(UserEvent.UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ in mopl-dm: {}", event);
        ExternalUserView userView = externalUserViewRepository.findById(event.userId())
                .orElseGet(() -> ExternalUserView.create(event.userId(), event.name(), event.imageKey()));
        userView.update(event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }
}
