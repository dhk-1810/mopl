package org.codeit.sb06.team03.mopl.notification.infra.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String PLAYLIST_SUBSCRIBED_QUEUE = "mopl.notification.playlist-subscribed.queue";
    public static final String PLAYLIST_SUBSCRIBED_ROUTING_KEY = "playlist.subscribed";

    @Bean
    public Queue playlistSubscribedQueue() {
        return new Queue(PLAYLIST_SUBSCRIBED_QUEUE, true);
    }

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public Binding bindingPlaylistSubscribed(Queue playlistSubscribedQueue, TopicExchange playlistExchange) {
        return BindingBuilder.bind(playlistSubscribedQueue)
                .to(playlistExchange)
                .with(PLAYLIST_SUBSCRIBED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
