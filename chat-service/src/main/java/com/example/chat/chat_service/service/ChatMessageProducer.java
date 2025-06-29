package com.example.chat.chat_service.service;

import com.example.chat.chat_service.model.ChatMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ChatMessageProducer, chat mesajlarını RabbitMQ üzerinden yayınlamak için kullanılır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Service: Bu sınıfın bir servis (iş katmanı) olduğunu belirtir.</li>
 *   <li>RabbitTemplate: Spring'in RabbitMQ ile mesaj göndermek için kullandığı yardımcı sınıftır.</li>
 *   <li>EXCHANGE ve ROUTING_KEY: Mesajların hangi exchange ve routing key ile gönderileceğini belirler.</li>
 * </ul>
 */
@Service
public class ChatMessageProducer {
    /**
     * Mesajların gönderileceği RabbitMQ exchange adı.
     */
    private static final String EXCHANGE = "chat.exchange";
    /**
     * Mesajların yönlendirileceği routing key.
     */
    private static final String ROUTING_KEY = "chat.message";

    /**
     * Mesajları RabbitMQ'ya göndermek için kullanılan Spring bileşeni.
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Bir chat mesajını RabbitMQ'ya gönderir.
     * <p>
     * <b>Nasıl çalışır?</b> Mesaj, belirlenen exchange ve routing key ile RabbitMQ'ya iletilir.
     * </p>
     * @param message Gönderilecek chat mesajı
     */
    public void sendMessage(ChatMessage message) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
    }
}
