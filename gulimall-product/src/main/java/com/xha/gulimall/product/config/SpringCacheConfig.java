package com.xha.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class SpringCacheConfig {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
//        1.首先创建RedisCacheConfiguration对象
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig();
//        2.指定key的序列化器为String类型
        cacheConfig = cacheConfig
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
//        3.指定value的序列化器为Json类型
        cacheConfig = cacheConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//        4.CacheProperties的作用就是读取配置文件中的配置，将配置文件中的所有配置都生效
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        if (redisProperties.getTimeToLive() != null){
            cacheConfig = cacheConfig.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null){
            cacheConfig = cacheConfig.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isUseKeyPrefix()){
            cacheConfig = cacheConfig.disableKeyPrefix();
        }
        if (!redisProperties.isCacheNullValues()){
            cacheConfig = cacheConfig.disableCachingNullValues();
        }

        return cacheConfig;
    }
}
