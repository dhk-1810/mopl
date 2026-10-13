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

    public static final String WATCHING_SESSION_EXCHANGE = "mopl.watching-session.exchange";
    public static final String WATCHING_SESSION_DELETE_ROUTING_KEY = "mopl.watching-session.delete";
    public static final String WATCHING_SESSION_DELETE_QUEUE = "mopl.live-chat.watching-session-delete.queue";

    @Bean
    public TopicExchange watchingSessionExchange() {
        return new TopicExchange(WATCHING_SESSION_EXCHANGE);
    }

    @Bean
    public Queue watchingSessionDeleteQueue() {
        return new Queue(WATCHING_SESSION_DELETE_QUEUE, true);
    }

    @Bean
    public Binding watchingSessionDeleteBinding(Queue watchingSessionDeleteQueue, TopicExchange watchingSessionExchange) {
        return BindingBuilder.bind(watchingSessionDeleteQueue)
                .to(watchingSessionExchange)
                .with(WATCHING_SESSION_DELETE_ROUTING_KEY);
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
                log.info("[Publisher Confirm] Broker successfully received and persisted message in mopl-live-chat. id: {}", msgId);
            } else {
                log.error("[Publisher Confirm] Broker NACK/FAILED in mopl-live-chat. id: {}, cause: {}", msgId, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("[Publisher Returns] Message unroutable in mopl-live-chat: replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });
        return rabbitTemplate;
    }
}
