package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.ExternalUserViewRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_PROFILE_EVENTS, groupId = "content-profile-group")
    @Transactional(value = "contentTransactionManager")
    public void handleProfileEventsFromKafka(String payload) {
        log.info("Received ProfileKafkaEvent from Kafka in mopl-content: {}", payload);
        try {
            ProfileKafkaEvent event = objectMapper.readValue(payload, ProfileKafkaEvent.class);
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
            log.error("Failed to process ProfileKafkaEvent in mopl-content: {}", e.getMessage(), e);
        }
    }
}
