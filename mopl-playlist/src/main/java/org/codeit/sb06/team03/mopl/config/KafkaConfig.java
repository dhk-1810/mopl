package org.codeit.sb06.team03.mopl.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_SAGA_START = "content.saga.delete.start";
    public static final String TOPIC_SAGA_RESPONSE = "content.saga.delete.response";
    public static final String TOPIC_USER_PROFILE_EVENTS = "user.profile.events";
    public static final String TOPIC_CONTENT_METADATA_EVENTS = "content.metadata.events";
    public static final String TOPIC_USER_FOLLOW_EVENTS = "user.follow.events";
}
