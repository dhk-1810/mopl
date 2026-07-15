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

    public static final String USER_EXCHANGE = "mopl.user.exchange";
    public static final String USER_ROLE_UPDATED_QUEUE = "notification.user-role-updated.queue";
    public static final String USER_ROLE_UPDATED_ROUTING_KEY = "user.role-updated";
    public static final String USER_FOLLOWED_QUEUE = "notification.user-followed.queue";
    public static final String USER_FOLLOWED_ROUTING_KEY = "user.followed";

    public static final String DM_EXCHANGE = "mopl.dm.exchange";
    public static final String DM_NOTIFICATION_REQUIRED_QUEUE = "notification.dm-notification-required.queue";
    public static final String DM_NOTIFICATION_REQUIRED_ROUTING_KEY = "dm.notification-required";

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

    // User 관련 빈
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userRoleUpdatedQueue() {
        return new Queue(USER_ROLE_UPDATED_QUEUE, true);
    }

    @Bean
    public Binding bindingUserRoleUpdated() {
        return BindingBuilder.bind(userRoleUpdatedQueue())
                .to(userExchange())
                .with(USER_ROLE_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Queue userFollowedQueue() {
        return new Queue(USER_FOLLOWED_QUEUE, true);
    }

    @Bean
    public Binding bindingUserFollowed() {
        return BindingBuilder.bind(userFollowedQueue())
                .to(userExchange())
                .with(USER_FOLLOWED_ROUTING_KEY);
    }

    // DM 관련 빈
    @Bean
    public TopicExchange dmExchange() {
        return new TopicExchange(DM_EXCHANGE);
    }

    @Bean
    public Queue dmNotificationRequiredQueue() {
        return new Queue(DM_NOTIFICATION_REQUIRED_QUEUE, true);
    }

    @Bean
    public Binding bindingDmNotificationRequired() {
        return BindingBuilder.bind(dmNotificationRequiredQueue())
                .to(dmExchange())
                .with(DM_NOTIFICATION_REQUIRED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

