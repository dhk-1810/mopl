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
    @Transactional(value = "playlistTransactionManager")
    public void handleProfileCreated(UserEvent.UserProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ: {}", event);
        ExternalUserView userView = ExternalUserView.create(event.getUserId(), event.getName(), event.getImageKey());
        externalUserViewRepository.save(userView);
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    @Transactional(value = "playlistTransactionManager")
    public void handleProfileUpdated(UserEvent.UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ: {}", event);
        ExternalUserView userView = externalUserViewRepository.findById(event.getUserId())
                .orElseGet(() -> ExternalUserView.create(event.getUserId(), event.getName(), event.getImageKey()));
        userView.update(event.getName(), event.getImageKey());
        externalUserViewRepository.save(userView);
    }
}
