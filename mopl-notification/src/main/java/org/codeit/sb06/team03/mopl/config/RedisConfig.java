package org.codeit.sb06.team03.mopl.config;

import org.codeit.sb06.team03.mopl.sse.NotificationInstanceId;
import org.codeit.sb06.team03.mopl.sse.listener.NotificationSseRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Bean
    public MessageListenerAdapter notificationSseListenerAdapter(NotificationSseRedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter notificationSseListenerAdapter,
            NotificationInstanceId instanceId
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                notificationSseListenerAdapter,
                new ChannelTopic("notification:instance:" + instanceId.getId())
        );
        return container;
    }
}
