package com.example.chat.chat_service;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * CacheConfig, uygulamanın Redis tabanlı önbellek (cache) altyapısını yapılandırır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Configuration: Bu sınıfın bir konfigürasyon sınıfı olduğunu belirtir.</li>
 *   <li>@EnableCaching: Spring Boot'ta önbellek desteğini etkinleştirir.</li>
 *   <li>RedisCacheManager: Redis ile cache yönetimini sağlar.</li>
 *   <li>cacheManager metodu, Redis bağlantısı ile bir cache yöneticisi oluşturur.</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {
    /**
     * Redis tabanlı cache yöneticisi bean'i.
     * <p>
     * <b>Nasıl çalışır?</b> RedisConnectionFactory ile Redis'e bağlanır ve cache işlemlerini yönetir.
     * </p>
     * @param connectionFactory Redis bağlantı fabrikası
     * @return RedisCacheManager nesnesi
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory).build();
    }
}