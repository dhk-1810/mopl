package org.codeit.sb06.team03.mopl.config;

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

    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String PLAYLIST_SUBSCRIBED_ROUTING_KEY = "mopl.playlist.subscribed";
    public static final String CURATION_ADDED_ROUTING_KEY = "mopl.playlist.curation-added";

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        rabbitTemplate.setReplyTimeout(5000L); // RPC 응답 대기 5초
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            String msgId = correlationData != null ? correlationData.getId() : "null";
            if (ack) {
                log.info("[Publisher Confirm] Broker successfully received and persisted message in mopl-playlist. id: {}", msgId);
            } else {
                log.error("[Publisher Confirm] Broker NACK/FAILED in mopl-playlist. id: {}, cause: {}", msgId, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> {
            log.warn("[Publisher Returns] Message unroutable in mopl-playlist: replyCode={}, replyText={}, exchange={}, routingKey={}, message={}",
                    returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), returned.getMessage());
        });
        return rabbitTemplate;
    }
}
