package com.example.chat.chat_service.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.chat.chat_service.model.Conversation;
import com.example.chat.chat_service.model.FriendRequest;
import com.example.chat.chat_service.repository.ConversationRepository;
import com.example.chat.chat_service.repository.FriendRequestRepository;

/**
 * UserController, kullanıcıların arkadaşlık ve sohbet işlemlerini yöneten REST API controller'ıdır.
 *
 * <p>Yeni başlayanlar için açıklama:</p>
 * <ul>
 *   <li>@RestController: Bu sınıfın bir REST API controller olduğunu belirtir.</li>
 *   <li>@RequestMapping: Tüm endpoint'lerin "/api/user" ile başladığını belirtir.</li>
 *   <li>@CrossOrigin: Frontend'in (React) bu API'ye erişebilmesi için CORS ayarı yapılır.</li>
 *   <li>Autowired repository ve servisler, veritabanı işlemleri ve WebSocket bildirimleri için kullanılır.</li>
 *   <li>Her endpoint ve yardımcı fonksiyon, ilgili işlemin ne yaptığını açıklayan yorumlarla detaylandırılmıştır.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {
    /**
     * Arkadaşlık istekleriyle ilgili veritabanı işlemleri için repository.
     */
    @Autowired
    private FriendRequestRepository friendRequestRepository;
    /**
     * Kullanıcılar arası sohbetleri yöneten repository.
     */
    @Autowired
    private ConversationRepository conversationRepository;
    /**
     * WebSocket üzerinden frontend'e bildirim göndermek için kullanılır.
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    /**
     * Mesaj işlemleri için repository.
     */
    @Autowired
    private com.example.chat.chat_service.repository.ChatMessageRepository chatMessageRepository;

    /**
     * Diğer servislerle REST API üzerinden iletişim kurmak için kullanılır.
     */
    private final RestTemplate restTemplate = new RestTemplate();
    /**
     * Kimlik doğrulama servisi URL'i.
     * Docker Compose ortamında servis adı kullanılmalıdır.
     */
    private final String AUTH_SERVICE_URL = "http://auth-service:8080/api/auth";

    /**
     * Kullanıcının var olup olmadığını kontrol eder (auth-service'e REST çağrısı yapar).
     * @param username Kontrol edilecek kullanıcı adı
     * @return Kullanıcı varsa true, yoksa false
     */
    private boolean userExists(String username) {
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(
                AUTH_SERVICE_URL + "/user-exists/" + username, Boolean.class);
            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bir kullanıcıdan diğerine arkadaşlık isteği gönderir.
     * Aynı kullanıcıya tekrar istek gönderilmesini ve bekleyen isteği engeller.
     * WebSocket ile bildirim gönderir.
     *
     * @param fromUsername İsteği gönderen kullanıcı
     * @param toUsername İsteği alan kullanıcı
     * @return Sonuç mesajı
     */
    @PostMapping("/friend-request/{toUsername}")
    public String sendFriendRequest(@RequestParam String fromUsername, @PathVariable String toUsername) {
        if (!userExists(fromUsername) || !userExists(toUsername)) return "Kullanıcı bulunamadı";
        // Aynı isteği tekrar gönderme
        if (friendRequestRepository.findAll().stream().anyMatch(r ->
                ((r.getFromUsername().equalsIgnoreCase(fromUsername) && r.getToUsername().equalsIgnoreCase(toUsername)) ||
                 (r.getFromUsername().equalsIgnoreCase(toUsername) && r.getToUsername().equalsIgnoreCase(fromUsername))) &&
                r.getStatus() == FriendRequest.Status.PENDING)) {
            return "Zaten bekleyen bir istek var";
        }
        FriendRequest req = new FriendRequest();
        req.setFromUsername(fromUsername);
        req.setToUsername(toUsername);
        req.setStatus(FriendRequest.Status.PENDING);
        friendRequestRepository.save(req);
        // WebSocket ile bildirim gönder
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + toUsername, req);
        return "İstek gönderildi";
    }

    /**
     * Bir arkadaşlık isteğini kabul eder ve iki kullanıcı arasında otomatik olarak sohbet (conversation) oluşturur.
     * WebSocket ile her iki kullanıcıya bildirim gönderir.
     *
     * @param requestId Kabul edilecek istek ID'si
     * @return Sonuç mesajı
     */
    @PostMapping("/friend-request/{requestId}/accept")
    public String acceptFriendRequest(@PathVariable Long requestId) {
        Optional<FriendRequest> reqOpt = friendRequestRepository.findById(requestId);
        if (reqOpt.isEmpty()) return "İstek bulunamadı";
        FriendRequest req = reqOpt.get();
        req.setStatus(FriendRequest.Status.ACCEPTED);
        friendRequestRepository.save(req);
        // Otomatik conversation oluştur
        if (conversationRepository.findAll().stream().noneMatch(c ->
                (c.getUser1().equalsIgnoreCase(req.getFromUsername()) && c.getUser2().equalsIgnoreCase(req.getToUsername())) ||
                (c.getUser1().equalsIgnoreCase(req.getToUsername()) && c.getUser2().equalsIgnoreCase(req.getFromUsername())))) {
            Conversation conv = new Conversation();
            conv.setUser1(req.getFromUsername());
            conv.setUser2(req.getToUsername());
            conversationRepository.save(conv);
        }
        // WebSocket ile bildirim gönder
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + req.getFromUsername(), req);
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + req.getToUsername(), req);
        return "Arkadaş eklendi";
    }

    /**
     * Bir arkadaşlık isteğini reddeder ve WebSocket ile bildirim gönderir.
     *
     * @param requestId Reddedilecek istek ID'si
     * @return Sonuç mesajı
     */
    @PostMapping("/friend-request/{requestId}/reject")
    public String rejectFriendRequest(@PathVariable Long requestId) {
        Optional<FriendRequest> reqOpt = friendRequestRepository.findById(requestId);
        if (reqOpt.isEmpty()) return "İstek bulunamadı";
        FriendRequest req = reqOpt.get();
        req.setStatus(FriendRequest.Status.REJECTED);
        friendRequestRepository.save(req);
        // WebSocket ile bildirim gönder
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + req.getFromUsername(), req);
        messagingTemplate.convertAndSend("/topic/friend-request-status/" + req.getToUsername(), req);
        return "İstek reddedildi";
    }

    /**
     * Kullanıcının arkadaş listesini (accepted friend requests) döndürür.
     *
     * @param username Arkadaşları listelenecek kullanıcı
     * @return Kullanıcının arkadaşlarının isimleri
     */
    @GetMapping("/{username}/friends")
    public List<String> getFriends(@PathVariable String username) {
        return friendRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == FriendRequest.Status.ACCEPTED &&
                        (r.getFromUsername().equalsIgnoreCase(username) || r.getToUsername().equalsIgnoreCase(username)))
                .map(r -> r.getFromUsername().equalsIgnoreCase(username) ? r.getToUsername() : r.getFromUsername())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Kullanıcının bekleyen (pending) arkadaşlık isteklerini döndürür.
     *
     * @param username İstekleri listelenecek kullanıcı
     * @return Kullanıcıya gelen bekleyen FriendRequest nesneleri
     */
    @GetMapping("/{username}/friend-requests")
    public List<FriendRequest> getPendingRequests(@PathVariable String username) {
        return friendRequestRepository.findAll().stream()
                .filter(r -> r.getToUsername().equalsIgnoreCase(username) && r.getStatus() == FriendRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    /**
     * İki kullanıcı arasındaki arkadaşlığı ve ilgili sohbeti (conversation) siler.
     * Tüm mesajlar da silinir.
     *
     * @param username1 Birinci kullanıcı
     * @param username2 İkinci kullanıcı
     * @return Sonuç mesajı
     */
    @PostMapping("/remove-friend")
    public String removeFriend(@RequestParam String username1, @RequestParam String username2) {
        // İki kullanıcı arasındaki accepted friend request'i bul
        Optional<FriendRequest> reqOpt = friendRequestRepository.findAll().stream()
            .filter(r -> r.getStatus() == FriendRequest.Status.ACCEPTED &&
                ((r.getFromUsername().equalsIgnoreCase(username1) && r.getToUsername().equalsIgnoreCase(username2)) ||
                 (r.getFromUsername().equalsIgnoreCase(username2) && r.getToUsername().equalsIgnoreCase(username1))))
            .findFirst();
        if (reqOpt.isEmpty()) return "Arkadaşlık kaydı bulunamadı";
        friendRequestRepository.delete(reqOpt.get());
        // İlgili conversation'ı ve mesajlarını sil
        conversationRepository.findAll().stream()
            .filter(c -> (c.getUser1().equalsIgnoreCase(username1) && c.getUser2().equalsIgnoreCase(username2)) ||
                         (c.getUser1().equalsIgnoreCase(username2) && c.getUser2().equalsIgnoreCase(username1)))
            .findFirst().ifPresent(conv -> {
                chatMessageRepository.deleteAll(chatMessageRepository.findByConversationId(conv.getId()));
                conversationRepository.delete(conv);
            });
        return "Arkadaş silindi";
    }
}
