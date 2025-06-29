package com.example.chat.chat_service.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat.chat_service.model.ChatMessage;
import com.example.chat.chat_service.model.FriendRequest;
import com.example.chat.chat_service.repository.ChatMessageRepository;
import com.example.chat.chat_service.repository.FriendRequestRepository;

/**
 * ChatWebSocketController, WebSocket üzerinden gerçek zamanlı mesajlaşmayı yönetir.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@RestController: Bu sınıfın bir REST API controller olduğunu belirtir.</li>
 *   <li>SimpMessagingTemplate: WebSocket ile frontend'e mesaj göndermek için kullanılır.</li>
 *   <li>@MessageMapping: WebSocket üzerinden gelen mesajları dinler.</li>
 * </ul>
 */
@RestController
public class ChatWebSocketController {
    /**
     * WebSocket ile mesaj göndermek için kullanılan Spring bileşeni.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Mesajları veritabanına kaydetmek için kullanılan repository.
     */
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    /**
     * Kullanıcı arkadaşlık isteklerini yönetmek için kullanılan repository.
     */
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    /**
     * WebSocket ile gelen mesajı ilgili kanala publish eder.
     * <p>
     * <b>Nasıl çalışır?</b> Mesajın zamanını backend'de ayarlar, kaydeder ve ilgili kanala gönderir.
     * </p>
     * @param message Gönderilecek chat mesajı
     */
    @MessageMapping("/chat.send")
    public void send(ChatMessage message) {
        // Timestamp backend tarafından atanmalı
        message.setTimestamp(LocalDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);
        // Sadece ilgili kanala publish et
        if (saved.getChannel() != null && !saved.getChannel().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/messages/" + saved.getChannel(), saved);
        }
    }

    /**
     * Belirli bir kanala mesaj göndermek için yardımcı metot.
     * @param channel Kanal adı
     * @param message Gönderilecek mesaj
     */
    public void sendToChannel(String channel, ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/messages/" + channel, message);
    }

    /**
     * REST ile gelen mesajı WebSocket ile publish etmek için yardımcı metot.
     * @param message Gönderilecek mesaj
     */
    public void publishMessage(ChatMessage message) {
        if (message.getChannel() != null && !message.getChannel().isEmpty()) {
            messagingTemplate.convertAndSend("/topic/messages/" + message.getChannel(), message);
        }
    }

    /**
     * Kullanıcı bir friend-request kanalına subscribe olduğunda,
     * hem bekleyen (pending) hem de kullanıcının gönderdiği (sent) friend request'leri WebSocket ile gönderir.
     * Böylece frontend abone olur olmaz en güncel durumu alır.
     */
    @EventListener
    public void handleSessionSubscribeEvent(org.springframework.messaging.simp.stomp.StompHeaderAccessor event) {
        String destination = event.getDestination();
        if (destination != null && destination.startsWith("/topic/friend-request-status/")) {
            String username = destination.substring(destination.lastIndexOf("/") + 1);
            // Kısa bir gecikme ekle (frontend handler'ın hazır olması için)
            try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            // Bekleyen (pending) istekler
            List<FriendRequest> pending = friendRequestRepository.findByToUsernameAndStatus(username, com.example.chat.chat_service.model.FriendRequest.Status.PENDING);
            // Kullanıcının gönderdiği (sent) istekler
            List<FriendRequest> sent = friendRequestRepository.findByFromUsernameAndStatus(username, com.example.chat.chat_service.model.FriendRequest.Status.PENDING);
            // Tümünü tek bir nesneyle gönder (ör: Map veya özel bir DTO ile)
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("pending", pending);
            payload.put("sent", sent);
            messagingTemplate.convertAndSend(destination, payload);
        }
    }

    /**
     * Frontend subscribe olduktan sonra WebSocket ile friend request listesini anlık gönderen endpoint.
     * Frontend: stompClient.publish({ destination: "/app/friend-request.fetch", body: user.username })
     */
    @MessageMapping("/friend-request.fetch")
    public void fetchFriendRequests(String username) {
        List<FriendRequest> pending = friendRequestRepository.findByToUsernameAndStatus(username, com.example.chat.chat_service.model.FriendRequest.Status.PENDING);
        List<FriendRequest> sent = friendRequestRepository.findByFromUsernameAndStatus(username, com.example.chat.chat_service.model.FriendRequest.Status.PENDING);
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("pending", pending);
        payload.put("sent", sent);
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + username, payload);
    }
}
