package com.vantage.api.config;

import com.vantage.api.dto.LinkValidationTask;
import com.vantage.api.service.LinkWorkerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration // Tells Spring this is a configuration class, calls it when the app starts
public class RedisConfig {

    @Bean
    public RedisTemplate<String, LinkValidationTask> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, LinkValidationTask> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. String serialization for the key
        template.setKeySerializer(RedisSerializer.string());

        // 2. JSON serialization for the Value (DTO)
        template.setValueSerializer(RedisSerializer.json());

        return template;
    }

    // Listens to "link-validation" always, looking for when something comes into the channel
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // We listen to the exact same channel used in LeadService
        container.addMessageListener(listenerAdapter, new ChannelTopic("link-validation"));
        return container;
    }

    // Container passes JSON to here
    @Bean
    MessageListenerAdapter linkListenerAdapter(LinkWorkerService workerService) {
        // We tell Spring to call the "handleMessage" method in our worker service
        MessageListenerAdapter adapter = new MessageListenerAdapter(workerService, "handleMessage");

        // Use the standard JSON serializer
        adapter.setSerializer(RedisSerializer.json());
        return adapter;
    }

}
