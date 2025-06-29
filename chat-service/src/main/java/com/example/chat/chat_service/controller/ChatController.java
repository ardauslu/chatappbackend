package com.example.chat.chat_service.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.chat_service.model.ChatMessage;
import com.example.chat.chat_service.repository.ChatMessageRepository;
import com.example.chat.chat_service.service.ChatMessageProducer;

/**
 * ChatController, chat mesajlarının REST API üzerinden yönetilmesini sağlar.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@RestController: Bu sınıfın bir REST API controller olduğunu belirtir.</li>
 *   <li>@RequestMapping: Tüm endpoint'lerin "/api/chat" ile başladığını belirtir.</li>
 *   <li>Autowired repository ve servisler, veritabanı ve mesajlaşma işlemleri için kullanılır.</li>
 *   <li>Her endpoint ve yardımcı fonksiyon, ilgili işlemin ne yaptığını açıklayan yorumlarla detaylandırılmıştır.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    /**
     * Mesaj veritabanı işlemleri için repository.
     */
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Mesajları RabbitMQ'ya göndermek için kullanılan servis.
     */
    @Autowired
    private ChatMessageProducer chatMessageProducer;

    /**
     * WebSocket ile mesaj yayınlamak için kullanılan controller.
     */
    @Autowired
    private ChatWebSocketController chatWebSocketController;

    /**
     * Tüm mesajları sayfalı olarak döndürür.
     * @param page Sayfa numarası
     * @param size Sayfa başına mesaj sayısı
     * @return Sayfalı mesaj listesi
     */
    @GetMapping("/messages")
    // @Cacheable("messages") // CACHE KALDIRILDI
    public Page<ChatMessage> getAllMessages(@RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findAll(pageable);
    }

    /**
     * Yeni bir chat mesajı gönderir.
     * <p>
     * <b>Nasıl çalışır?</b> Mesajın zamanını ayarlar, RabbitMQ'ya gönderir ve WebSocket ile yayınlar.
     * </p>
     * @param message Gönderilecek chat mesajı
     */
    @PostMapping("/send")
    @CacheEvict(value = "messages", allEntries = true)
    public void sendMessage(@RequestBody ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatMessageProducer.sendMessage(message);
        chatWebSocketController.publishMessage(message);
    }

    /**
     * Belirli bir mesajı siler ve silindiğini WebSocket ile bildirir.
     * @param id Silinecek mesajın id'si
     */
    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = "messages", allEntries = true)
    public void deleteMessage(@PathVariable Long id) {
        chatMessageRepository.deleteById(id);
        // Kanal mesajı ise anlık silme bildirimi gönder
        chatWebSocketController.publishMessage(new ChatMessage() {{
            setId(id);
            setContent(null);
            setSender(null);
            setTimestamp(LocalDateTime.now());
            setChannel(null);
        }});
    }

    /**
     * Belirli bir mesajı günceller ve güncellendiğini WebSocket ile bildirir.
     * @param id Güncellenecek mesajın id'si
     * @param updatedMessage Yeni mesaj içeriği
     * @return Güncellenmiş mesaj
     */
    @PutMapping("/update/{id}")
    @CacheEvict(value = "messages", allEntries = true)
    public ChatMessage updateMessage(@PathVariable Long id, @RequestBody ChatMessage updatedMessage) {
        return chatMessageRepository.findById(id)
                .map(message -> {
                    message.setContent(updatedMessage.getContent());
                    message.setChannel(updatedMessage.getChannel());
                    ChatMessage saved = chatMessageRepository.save(message);
                    // Kanal mesajı ise anlık güncelleme bildirimi gönder
                    chatWebSocketController.publishMessage(saved);
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    /**
     * Belirli bir kanala ait mesajları sayfalı olarak döndürür.
     * @param channel Kanal adı
     * @param page Sayfa numarası
     * @param size Sayfa başına mesaj sayısı
     * @return Sayfalı mesaj listesi
     */
    @GetMapping("/messages/channel/{channel}")
    // @Cacheable("messages") // CACHE KALDIRILDI
    public Page<ChatMessage> getMessagesByChannel(@PathVariable String channel,
                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        System.out.println("Kanal adı (gelen): '" + channel + "'");
        Page<ChatMessage> result = chatMessageRepository.findByChannelIgnoreCaseTrimmed(channel, pageable);
        System.out.println("Dönen mesaj sayısı: " + result.getContent().size());
        return result;
    }

    /**
     * Belirli bir gönderene ait mesajları sayfalı olarak döndürür.
     * @param sender Gönderenin adı
     * @param page Sayfa numarası
     * @param size Sayfa başına mesaj sayısı
     * @return Sayfalı mesaj listesi
     */
    @GetMapping("/messages/sender/{sender}")
    // @Cacheable("messages") // CACHE KALDIRILDI
    public Page<ChatMessage> getMessagesBySender(@PathVariable String sender,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findBySender(sender, pageable);
    }
}
