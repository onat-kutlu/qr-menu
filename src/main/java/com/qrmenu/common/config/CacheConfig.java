package com.qrmenu.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis/Cache configuration
 * 
 * Spring Boot will auto-configure LettuceConnectionFactory based on:
 * - spring.redis.host
 * - spring.redis.port
 * - spring.redis.lettuce.pool.* properties
 * 
 * This config just customizes the RedisTemplate serialization
 */
@Configuration
public class CacheConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        template.setConnectionFactory(connectionFactory);
        
        // String serializer for keys and values
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        
        // Set key-value serializers
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        
        // Set hash key-value serializers
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }
}
