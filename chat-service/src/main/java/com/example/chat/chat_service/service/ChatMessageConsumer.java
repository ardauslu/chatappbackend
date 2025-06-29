package com.example.chat.chat_service.service;

import com.example.chat.chat_service.model.ChatMessage;
import com.example.chat.chat_service.repository.ChatMessageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ChatMessageConsumer, RabbitMQ kuyruğundan gelen chat mesajlarını dinler ve veritabanına kaydeder.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Service: Bu sınıfın bir servis (iş katmanı) olduğunu belirtir.</li>
 *   <li>@RabbitListener: Belirtilen RabbitMQ kuyruğunu dinler ve mesaj geldiğinde ilgili metodu çalıştırır.</li>
 *   <li>ChatMessageRepository: Mesajları veritabanına kaydetmek için kullanılır.</li>
 * </ul>
 */
@Service
public class ChatMessageConsumer {
    /**
     * Chat mesajlarını veritabanına kaydetmek için kullanılan repository.
     */
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * RabbitMQ kuyruğundan gelen mesajı alır ve veritabanına kaydeder.
     * <p>
     * <b>Nasıl çalışır?</b> Kuyruğa yeni bir mesaj geldiğinde bu metot otomatik olarak çağrılır.
     * </p>
     * @param message Kuyruktan alınan chat mesajı
     */
    @RabbitListener(queues = "chat.queue")
    public void receiveMessage(ChatMessage message) {
        chatMessageRepository.save(message);
    }
}
