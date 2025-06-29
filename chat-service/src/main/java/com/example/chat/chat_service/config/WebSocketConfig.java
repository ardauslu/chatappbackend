package com.example.chat.chat_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocketConfig, uygulamanın WebSocket mesajlaşma altyapısını yapılandırır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Configuration: Bu sınıfın bir konfigürasyon sınıfı olduğunu belirtir.</li>
 *   <li>@EnableWebSocketMessageBroker: WebSocket mesajlaşma desteğini etkinleştirir.</li>
 *   <li>configureMessageBroker: Mesajların hangi prefix ile yönlendirileceğini ve broker'ın nasıl çalışacağını ayarlar.</li>
 *   <li>registerStompEndpoints: WebSocket bağlantı noktalarını (endpoint) tanımlar.</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * Mesaj broker'ı yapılandırılır. /topic ile başlayan mesajlar frontend'e iletilir.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket endpoint'i tanımlanır. /ws ile bağlantı kurulur.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
