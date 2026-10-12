package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.config.RabbitConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalUserView;
import org.codeit.sb06.team03.mopl.repository.ExternalUserViewRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileEventListener {

    private final ExternalUserViewRepository externalUserViewRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_CREATE_QUEUE)
    @Transactional(value = "contentTransactionManager")
    public void handleProfileCreated(
            ProfileCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received UserProfileCreatedEvent from RabbitMQ in mopl-content: {}", event);
        try {
            ExternalUserView userView = ExternalUserView.create(event.userId(), event.name(), event.imageKey());
            externalUserViewRepository.save(userView);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to handle UserProfileCreatedEvent: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @RabbitListener(queues = RabbitConfig.USER_PROFILE_UPDATE_QUEUE)
    @Transactional(value = "contentTransactionManager")
    public void handleProfileUpdated(
            ProfileUpdatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        log.info("Received ProfileUpdatedEvent from RabbitMQ in mopl-content: {}", event);
        try {
            ExternalUserView userView = externalUserViewRepository.findById(event.userId())
                    .orElseGet(() -> ExternalUserView.create(event.userId(), event.name(), event.imageKey()));
            userView.update(event.name(), event.imageKey());
            externalUserViewRepository.save(userView);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to handle ProfileUpdatedEvent: {}", e.getMessage(), e);
            channel.basicReject(deliveryTag, false);
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_PROFILE_EVENTS, groupId = "content-profile-group")
    @Transactional(value = "contentTransactionManager")
    public void handleProfileEventsFromKafka(String payload) {
        log.info("Received UserProfileKafkaEvent from Kafka in mopl-content: {}", payload);
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
            log.error("Failed to process UserProfileKafkaEvent in mopl-content: {}", e.getMessage(), e);
        }
    }
}
