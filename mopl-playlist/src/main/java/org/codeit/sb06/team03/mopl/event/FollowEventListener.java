package org.codeit.sb06.team03.mopl.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codeit.sb06.team03.mopl.config.KafkaConfig;
import org.codeit.sb06.team03.mopl.entity.cqrs.ExternalFollowView;
import org.codeit.sb06.team03.mopl.repository.cqrs.ExternalFollowRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventListener {

    private final ExternalFollowRepository externalFollowRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_FOLLOW_EVENTS, groupId = "playlist-follow-group")
    @Transactional(value = "playlistTransactionManager")
    public void handleFollowEvents(String payload) {
        log.info("Received FollowKafkaEvent from Kafka in mopl-playlist: {}", payload);
        try {
            FollowKafkaEvent event = objectMapper.readValue(payload, FollowKafkaEvent.class);
            if ("FOLLOWED".equals(event.eventType())) {
                ExternalFollowView followView = ExternalFollowView.create(event.followeeId(), event.followerId());
                externalFollowRepository.save(followView);
                log.info("Saved ExternalFollowView: followeeId={}, followerId={}", event.followeeId(), event.followerId());
            } else if ("UNFOLLOWED".equals(event.eventType())) {
                externalFollowRepository.deleteById(new ExternalFollowView.ExternalFollowId(event.followeeId(), event.followerId()));
                log.info("Deleted ExternalFollowView: followeeId={}, followerId={}", event.followeeId(), event.followerId());
            }
        } catch (Exception e) {
            log.error("Failed to handle FollowKafkaEvent for payload: {}", payload, e);
        }
    }
}
