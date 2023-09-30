package com.framework.security.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @version V1.0
 * @program: teamwork
 * @package: com.framework.security.config
 * @description: Redisconfiguration class
 * @create: 2020-06-25 10:34
 **/
@Configuration
public class RedisConfig extends CachingConfigurerSupport {
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Customize the springSessionDefaultRedisSerializer object, which will replace the default SESSION serialization object.
     * The default is JdkSerializationRedisSerializer, the disadvantage is that the class needs to implement the Serializable interface.
     * And if there is an exception during deserialization, a SerializationException will be thrown,
     * However, SessionRepositoryFilter does not handle exceptions, so if serialization exceptions will result in request exceptions
     */

    @Bean(name = "springSessionDefaultRedisSerializer")
    public GenericJackson2JsonRedisSerializer getGenericJackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    /**
     * The difference between JacksonJsonRedisSerializer and GenericJackson2JsonRedisSerializer:
     * GenericJackson2JsonRedisSerializer adds @class attribute to json, the full path package name of the class, which is convenient for deserialization.
     * JacksonJsonRedisSerializer If the List is stored, when it is deserialized,
     * If TypeReference is not specified, an error java.util.LinkedHashMap cannot be cast will be reported.
     *
     * @return
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Replace default serialization with Jackson2JsonRedisSerialize
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // Set the serialization rules of value and the serialization rules of key
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}