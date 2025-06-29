package com.example.chat.chat_service.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
import org.springframework.web.server.ResponseStatusException;

import com.example.chat.chat_service.model.ChatMessage;
import com.example.chat.chat_service.model.Conversation;
import com.example.chat.chat_service.repository.ChatMessageRepository;
import com.example.chat.chat_service.repository.ConversationRepository;
import com.example.chat.chat_service.repository.UserRepository;

/**
 * ConversationController, kullanıcılar arasındaki birebir sohbetlerin ve bu sohbetlere ait mesajların yönetimini sağlar.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@RestController: Bu sınıfın bir REST API controller olduğunu belirtir.</li>
 *   <li>@RequestMapping: Tüm endpoint'lerin "/api/conversation" ile başladığını belirtir.</li>
 *   <li>Autowired repository ve servisler, veritabanı ve mesajlaşma işlemleri için kullanılır.</li>
 *   <li>Her endpoint ve yardımcı fonksiyon, ilgili işlemin ne yaptığını açıklayan yorumlarla detaylandırılmıştır.</li>
 * </ul>
 */
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    /**
     * Conversation veritabanı işlemleri için repository.
     */
    @Autowired
    private ConversationRepository conversationRepository;
    /**
     * Chat mesajı işlemleri için repository.
     */
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    /**
     * Kullanıcı işlemleri için repository.
     */
    @Autowired
    private UserRepository userRepository;
    /**
     * WebSocket ile mesaj yayınlamak için kullanılan controller.
     */
    @Autowired
    private com.example.chat.chat_service.controller.ChatWebSocketController chatWebSocketController;
    /**
     * WebSocket ile frontend'e bildirim göndermek için kullanılır.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * Arkadaşlık isteği işlemleri için repository.
     */
    @Autowired
    private com.example.chat.chat_service.repository.FriendRequestRepository friendRequestRepository;

    /**
     * Belirli iki kullanıcı arasındaki conversation'ı getir (varsa).
     * @param username1 Birinci kullanıcı
     * @param username2 İkinci kullanıcı
     * @return Conversation nesnesi veya null
     */
    @GetMapping("/between/{username1}/{username2}")
    public Conversation getConversation(@PathVariable String username1, @PathVariable String username2) {
        if (username1 == null || username2 == null) return null;
        return conversationRepository.findByUser1AndUser2(username1, username2)
                .or(() -> conversationRepository.findByUser2AndUser1(username1, username2))
                .orElse(null);
    }

    /**
     * Bir conversation'ın mesajlarını getir (pagination).
     * @param conversationId Conversation id
     * @param page Sayfa numarası
     * @param size Sayfa başına mesaj sayısı
     * @return Sayfalı mesaj listesi
     */
    @GetMapping("/{conversationId}/messages")
    public Page<ChatMessage> getMessages(@PathVariable Long conversationId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findByConversationId(conversationId, pageable);
    }

    /**
     * Bir conversation'a mesaj gönder.
     * @param conversationId Conversation id
     * @param message Gönderilecek mesaj içeriği
     * @return Gönderilen mesaj nesnesi
     */
    @PostMapping("/{conversationId}/send")
    public ChatMessage sendMessage(@PathVariable Long conversationId, @RequestBody ChatMessage message) {
        Conversation conv = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sohbet bulunamadı veya silinmiş."));
        String sender = message.getSender();
        String receiver = conv.getUser1().equalsIgnoreCase(sender) ? conv.getUser2() : conv.getUser1();
        // Arkadaşlık kontrolü
        boolean isFriend = friendRequestRepository.findAll().stream()
            .anyMatch(r -> r.getStatus() == com.example.chat.chat_service.model.FriendRequest.Status.ACCEPTED &&
                ((r.getFromUsername().equalsIgnoreCase(sender) && r.getToUsername().equalsIgnoreCase(receiver)) ||
                 (r.getFromUsername().equalsIgnoreCase(receiver) && r.getToUsername().equalsIgnoreCase(sender))));
        if (!isFriend) {
            throw new RuntimeException("Kullanıcılar artık arkadaş değil, mesaj gönderilemez.");
        }
        message.setConversation(conv);
        message.setTimestamp(LocalDateTime.now());
        // Eğer channel alanı null veya boşsa, birebir sohbetler için varsayılan değer ata
        if (message.getChannel() == null || message.getChannel().isEmpty()) {
            message.setChannel(""); // veya "direct" yazabilirsiniz
        }
        ChatMessage saved = chatMessageRepository.save(message);
        // WebSocket ile conversation kanalına publish et
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, saved);
        // Eğer kanal varsa eski WebSocket publish de çalışsın
        if (saved.getChannel() != null && !saved.getChannel().isEmpty()) {
            chatWebSocketController.publishMessage(saved);
        }
        return saved;
    }

    /**
     * Yeni sohbet başlat (varsa mevcut olanı döndür).
     * @param body Kullanıcı bilgileri (user1, user2)
     * @return Oluşturulan veya bulunan Conversation nesnesi
     */
    @PostMapping("/start")
    public Conversation createConversation(@RequestBody Map<String, String> body) {
        String user1 = body.get("user1");
        String user2 = body.get("user2");
        if (user1 == null || user2 == null) throw new IllegalArgumentException("user1 ve user2 zorunlu");
        // Zaten varsa onu döndür
        Optional<Conversation> existing = conversationRepository.findByUser1AndUser2(user1, user2)
            .or(() -> conversationRepository.findByUser2AndUser1(user1, user2));
        if (existing.isPresent()) return existing.get();
        // Yoksa yeni oluştur
        Conversation conv = new Conversation();
        conv.setUser1(user1);
        conv.setUser2(user2);
        return conversationRepository.save(conv);
    }

    /**
     * Mevcut bir mesajı güncelle.
     * @param conversationId Conversation id
     * @param messageId Güncellenecek mesajın id'si
     * @param body Yeni mesaj içeriği
     * @return Güncellenen mesaj nesnesi
     */
    @PutMapping("/{conversationId}/update/{messageId}")
    public ChatMessage updateMessage(@PathVariable Long conversationId, @PathVariable Long messageId, @RequestBody Map<String, String> body) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesaj bulunamadı."));
        if (!message.getConversation().getId().equals(conversationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesaj bu sohbete ait değil.");
        }
        String newContent = body.get("content");
        if (newContent == null || newContent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yeni mesaj içeriği boş olamaz.");
        }
        message.setContent(newContent);
        ChatMessage updated = chatMessageRepository.save(message);
        // WebSocket ile conversation kanalına publish et (anlık güncelleme için)
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, updated);
        return updated;
    }

    /**
     * Belirli bir mesajı sil.
     * @param conversationId Conversation id
     * @param messageId Silinecek mesajın id'si
     */
    @DeleteMapping("/{conversationId}/delete/{messageId}")
    public void deleteMessage(@PathVariable Long conversationId, @PathVariable Long messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesaj bulunamadı."));
        if (!message.getConversation().getId().equals(conversationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesaj bu sohbete ait değil.");
        }
        chatMessageRepository.delete(message);
        // WebSocket ile conversation kanalına silinen mesajı publish et
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, Map.of(
            "type", "delete",
            "messageId", messageId
        ));
    }
}
