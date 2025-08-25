package com.example.identity_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // Sửa host/port/password theo môi trường của bạn
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        config.setPassword("mysecret");
        return new LettuceConnectionFactory(config);
    }

    // Template thao tác chuỗi & ZSET/SET/HASH
    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // Template số nguyên (nếu cần), để bạn linh hoạt (không bắt buộc)
    @Bean
    public RedisTemplate<String, Integer> intRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);
        t.setKeySerializer(new StringRedisSerializer());
        t.setValueSerializer(new GenericToStringSerializer<>(Integer.class));
        return t;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new PatternTopic("__keyevent@0__:expired"));
        return container;
    }
}


