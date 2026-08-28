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

    public static final String WS_EXCHANGE = "mopl.watching-session.exchange";
    public static final String CONTENT_EXCHANGE = "mopl.content.exchange";

    public static final String WS_CREATE_ROUTING = "watching-session.create";
    public static final String WS_DELETE_ROUTING = "watching-session.delete";
    public static final String ROUTING_KEY_SAGA_START = "content.saga.delete.start";
    public static final String ROUTING_KEY_SAGA_RESPONSE = "content.saga.delete.response";

    public static final String WS_CREATE_QUEUE = "watching-session.create.queue";
    public static final String WS_DELETE_QUEUE = "watching-session.delete.queue";
    public static final String WS_CONTENT_SAGA_START_QUEUE = "watching-session.content-saga-start.queue";

    @Bean
    public TopicExchange wsExchange() {
        return new TopicExchange(WS_EXCHANGE);
    }

    @Bean
    public Queue wsCreateQueue() {
        return new Queue(WS_CREATE_QUEUE, true);
    }

    @Bean
    public Queue wsDeleteQueue() {
        return new Queue(WS_DELETE_QUEUE, true);
    }

    @Bean
    public Binding wsCreateBinding() {
        return BindingBuilder.bind(wsCreateQueue())
                .to(wsExchange())
                .with(WS_CREATE_ROUTING);
    }

    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE);
    }

    @Bean
    public Queue wsContentSagaStartQueue() {
        return new Queue(WS_CONTENT_SAGA_START_QUEUE, true);
    }

    @Bean
    public Binding wsContentSagaStartBinding() {
        return BindingBuilder.bind(wsContentSagaStartQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_SAGA_START);
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
                log.info("[Publisher Confirm] Broker successfully received and persisted message in mopl-watching-session. id: {}", msgId);
            } else {
                log.error("[Publisher Confirm] Broker NACK/FAILED in mopl-watching-session. id: {}, cause: {}", msgId, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("[Publisher Returns] Message unroutable in mopl-watching-session: replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });
        return rabbitTemplate;
    }
}
