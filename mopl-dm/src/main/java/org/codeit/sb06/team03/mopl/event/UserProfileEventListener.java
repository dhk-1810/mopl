package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalUserViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserProfileEventListener {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    @Transactional(value = "dmTransactionManager")
    public void handleProfileCreated(UserProfileCreatedEvent event) {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-dm: {}", event);
        ExternalUserView userView = ExternalUserView.create(event.userId(), event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    @Transactional(value = "dmTransactionManager")
    public void handleProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("Received UserProfileUpdatedEvent from RabbitMQ in mopl-dm: {}", event);
        ExternalUserView userView = externalUserViewRepository.findById(event.userId())
                .orElseGet(() -> ExternalUserView.create(event.userId(), event.name(), event.imageKey()));
        userView.update(event.name(), event.imageKey());
        externalUserViewRepository.save(userView);
    }

    @KafkaListener(topics = "user.profile.events", groupId = "dm-profile-group")
    @Transactional(value = "dmTransactionManager")
    public void handleProfileEventsFromKafka(String payload) {
        log.info("Received UserProfileKafkaEvent from Kafka in mopl-dm: {}", payload);
        try {
            UserProfileKafkaEvent event = objectMapper.readValue(payload, UserProfileKafkaEvent.class);
            if ("CREATED".equals(event.eventType())) {
                ExternalUserView userView = ExternalUserView.create(event.userId(), event.name(), event.imageKey());
                externalUserViewRepository.save(userView);
            } else if ("UPDATED".equals(event.eventType())) {
                ExternalUserView userView = externalUserViewRepository.findById(event.userId())
                        .orElseGet(() -> ExternalUserView.create(event.userId(), event.name(), event.imageKey()));
                userView.update(event.name(), event.imageKey());
                externalUserViewRepository.save(userView);
            }
        } catch (Exception e) {
            log.error("Failed to process UserProfileKafkaEvent in mopl-dm: {}", e.getMessage(), e);
        }
    }
}
