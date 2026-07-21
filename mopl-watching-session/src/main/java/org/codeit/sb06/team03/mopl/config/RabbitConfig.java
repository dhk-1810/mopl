package org.codeit.sb06.team03.mopl.config;

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

    public static final String WS_EXCHANGE = "mopl.watching-session.exchange";

    public static final String WS_CREATE_ROUTING = "watching-session.create";
    public static final String WS_DELETE_ROUTING = "watching-session.delete";

    public static final String WS_CREATE_QUEUE = "watching-session.create.queue";
    public static final String WS_DELETE_QUEUE = "watching-session.delete.queue";

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
    public Binding wsDeleteBinding() {
        return BindingBuilder.bind(wsDeleteQueue())
                .to(wsExchange())
                .with(WS_DELETE_ROUTING);
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
