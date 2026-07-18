package org.codeit.sb06.team03.mopl.playlist;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Exchanges
    public static final String PLAYLIST_EXCHANGE = "mopl.playlist.exchange";
    public static final String CONTENT_EXCHANGE = "mopl.content.exchange";
    public static final String USER_EXCHANGE = "mopl.user.exchange";

    // Routing Keys
    public static final String ROUTING_KEY_CONTENT_CREATED = "content.created";
    public static final String ROUTING_KEY_CONTENT_UPDATED = "content.updated";
    public static final String ROUTING_KEY_CONTENT_DELETED = "content.deleted";

    public static final String ROUTING_KEY_PROFILE_CREATED = "user.profile-created";
    public static final String ROUTING_KEY_PROFILE_UPDATED = "user.profile-updated";

    // Queues
    public static final String CONTENT_CREATE_QUEUE = "playlist.content-create.queue";
    public static final String CONTENT_UPDATE_QUEUE = "playlist.content-update.queue";
    public static final String CONTENT_DELETE_QUEUE = "playlist.content-delete.queue";

    public static final String USER_PROFILE_CREATE_QUEUE = "playlist.user-profile-create.queue";
    public static final String USER_PROFILE_UPDATE_QUEUE = "playlist.user-profile-update.queue";

    @Bean
    public TopicExchange playlistExchange() {
        return new TopicExchange(PLAYLIST_EXCHANGE);
    }

    @Bean
    public TopicExchange contentExchange() {
        return new TopicExchange(CONTENT_EXCHANGE);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // Queues Beans
    @Bean
    public Queue contentCreateQueue() {
        return new Queue(CONTENT_CREATE_QUEUE, true);
    }

    @Bean
    public Queue contentUpdateQueue() {
        return new Queue(CONTENT_UPDATE_QUEUE, true);
    }

    @Bean
    public Queue contentDeleteQueue() {
        return new Queue(CONTENT_DELETE_QUEUE, true);
    }

    @Bean
    public Queue userProfileCreateQueue() {
        return new Queue(USER_PROFILE_CREATE_QUEUE, true);
    }

    @Bean
    public Queue userProfileUpdateQueue() {
        return new Queue(USER_PROFILE_UPDATE_QUEUE, true);
    }

    // Bindings Beans
    @Bean
    public Binding contentCreateBinding() {
        return BindingBuilder.bind(contentCreateQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_CONTENT_CREATED);
    }

    @Bean
    public Binding contentUpdateBinding() {
        return BindingBuilder.bind(contentUpdateQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_CONTENT_UPDATED);
    }

    @Bean
    public Binding contentDeleteBinding() {
        return BindingBuilder.bind(contentDeleteQueue())
                .to(contentExchange())
                .with(ROUTING_KEY_CONTENT_DELETED);
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
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
