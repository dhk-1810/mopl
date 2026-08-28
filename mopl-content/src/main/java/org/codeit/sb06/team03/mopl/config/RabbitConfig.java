package org.codeit.sb06.team03.mopl.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {

    public static final String CONTENT_EXCHANGE = "mopl.content.exchange";
    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String USER_EXCHANGE = "mopl.user.exchange";

    public static final String ROUTING_KEY_PROFILE_CREATED = "user.profile-created";
    public static final String ROUTING_KEY_PROFILE_UPDATED = "user.profile-updated";
    public static final String ROUTING_KEY_CURATION_CONTENT_REQUEST = "curation.content-request";
    public static final String ROUTING_KEY_CONTENT_BATCH_INFO = "content.batch-info";

    public static final String USER_PROFILE_CREATE_QUEUE = "content.user-profile-create.queue";
    public static final String USER_PROFILE_UPDATE_QUEUE = "content.user-profile-update.queue";
    public static final String CURATION_CONTENT_REQUEST_QUEUE = "content.curation-content-request.queue";
    public static final String CONTENT_BATCH_INFO_QUEUE = "content.batch-info.queue";

    public static final String ROUTING_KEY_SAGA_START = "content.saga.delete.start";
    public static final String ROUTING_KEY_SAGA_RESPONSE = "content.saga.delete.response";
    public static final String CONTENT_SAGA_RESPONSE_QUEUE = "content.saga-response.queue";
    public static final String CONTENT_SAGA_START_QUEUE = "playlist.content-saga-start.queue";

    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE);
    }

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userProfileCreateQueue() {
        return new Queue(USER_PROFILE_CREATE_QUEUE, true);
    }

    @Bean
    public Queue userProfileUpdateQueue() {
        return new Queue(USER_PROFILE_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue curationContentRequestQueue() {
        return new Queue(CURATION_CONTENT_REQUEST_QUEUE, true);
    }

    @Bean
    public Queue contentBatchInfoQueue() {
        return new Queue(CONTENT_BATCH_INFO_QUEUE, true);
    }

    @Bean
    public Binding userProfileCreateBinding() {
        return BindingBuilder.bind(userProfileCreateQueue())
                .to(userExchange())
                .with(ROUTING_KEY_PROFILE_CREATED);
    }

    @Bean
    public Binding userProfileUpdateBinding() {
        return BindingBuilder.bind(userProfileUpdateQueue())
                .to(userExchange())
                .with(ROUTING_KEY_PROFILE_UPDATED);
    }

    @Bean
    public Binding curationContentRequestBinding() {
        return BindingBuilder.bind(curationContentRequestQueue())
                .to(playlistExchange())
                .with(ROUTING_KEY_CURATION_CONTENT_REQUEST);
    }

    @Bean
    public Binding contentBatchInfoBinding() {
        return BindingBuilder.bind(contentBatchInfoQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_CONTENT_BATCH_INFO);
    }

    @Bean
    public Queue contentSagaResponseQueue() {
        return new Queue(CONTENT_SAGA_RESPONSE_QUEUE, true);
    }

    @Bean
    public Binding contentSagaResponseBinding() {
        return BindingBuilder.bind(contentSagaResponseQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_SAGA_RESPONSE);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String msgId = correlationData != null ? correlationData.getId() : "null";
            if (ack) {
                log.info("[Publisher Confirm] Broker successfully received and persisted message. id: {}", msgId);
            } else {
                log.error("[Publisher Confirm] Broker NACK/FAILED to persist message. id: {}, cause: {}", msgId, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("[Publisher Returns] Message unroutable: replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });
        return rabbitTemplate;
    }
}
