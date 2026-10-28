package org.codeit.sb06.team03.mopl.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_IMAGE_PRESIGNED_URL_EVENTS = "image.presigned-url.events";
    public static final String TOPIC_USER_PROFILE_EVENTS = "user.profile.events";
    public static final String TOPIC_USER_FOLLOW_EVENTS = "user.follow.events";

    @Bean
    public NewTopic userFollowEventsTopic() {
        return TopicBuilder.name(TOPIC_USER_FOLLOW_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
