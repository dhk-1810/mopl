package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalFollowView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalFollowRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final ExternalFollowRepository externalFollowRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_USER_FOLLOW_EVENTS,
            groupId = "playlist-follow-group",
            concurrency = "3"
    )
    @Transactional(value = "playlistTransactionManager")
    public void handleFollowEvents(
            String payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {
        log.info("[Partition: {}][Offset: {}][Key: {}][Thread: {}] Received FollowKafkaEvent: {}",
                partition, offset, key, Thread.currentThread().getName(), payload);
        try {
            FollowKafkaEvent event = objectMapper.readValue(payload, FollowKafkaEvent.class);
            if ("FOLLOWED".equals(event.eventType())) {
                ExternalFollowView followView = ExternalFollowView.create(event.followeeId(), event.followerId());
                externalFollowRepository.save(followView);
                log.info("[Partition: {}] Saved ExternalFollowView: followeeId={}, followerId={}", partition, event.followeeId(), event.followerId());
            } else if ("UNFOLLOWED".equals(event.eventType())) {
                externalFollowRepository.deleteById(new ExternalFollowView.ExternalFollowId(event.followeeId(), event.followerId()));
                log.info("[Partition: {}] Deleted ExternalFollowView: followeeId={}, followerId={}", partition, event.followeeId(), event.followerId());
            }
        } catch (Exception e) {
            log.error("Failed to handle FollowKafkaEvent for payload: {}", payload, e);
        }
    }
}
