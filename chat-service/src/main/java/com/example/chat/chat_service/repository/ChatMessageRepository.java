package com.example.chat.chat_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.chat.chat_service.model.ChatMessage;

/**
 * ChatMessageRepository arayüzü, ChatMessage (sohbet mesajı) nesneleriyle ilgili veritabanı işlemlerini gerçekleştirir.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>JpaRepository, temel CRUD işlemlerini otomatik olarak sağlar.</li>
 *   <li>Pageable parametresi ile sayfalama (paging) desteği sunar.</li>
 *   <li>Özel sorgu metotları, kanal, gönderen veya conversation id'ye göre mesaj aramayı kolaylaştırır.</li>
 *   <li>@Query ile özel SQL sorgusu yazılabilir.</li>
 * </ul>
 */
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    /**
     * Belirli bir kanaldaki mesajları sayfalı olarak döndürür.
     */
    Page<ChatMessage> findByChannel(String channel, Pageable pageable);
    /**
     * Belirli bir kanaldaki mesajları büyük/küçük harf duyarsız olarak döndürür.
     */
    Page<ChatMessage> findByChannelIgnoreCase(String channel, Pageable pageable);
    /**
     * Belirli bir kullanıcının gönderdiği mesajları döndürür.
     */
    Page<ChatMessage> findBySender(String sender, Pageable pageable);
    /**
     * Belirli bir conversation'a ait mesajları döndürür (sayfalı).
     */
    Page<ChatMessage> findByConversationId(Long conversationId, Pageable pageable);
    /**
     * Belirli bir conversation'a ait tüm mesajları liste olarak döndürür.
     */
    java.util.List<ChatMessage> findByConversationId(Long conversationId);
    /**
     * Kanal adını trimleyip küçük harfe çevirerek eşleşen mesajları döndürür (sayfalı).
     * @param channel Kanal adı
     * @param pageable Sayfalama parametresi
     * @return Kanal mesajları
     */
    @Query("SELECT m FROM ChatMessage m WHERE LOWER(TRIM(m.channel)) = LOWER(TRIM(:channel))")
    Page<ChatMessage> findByChannelIgnoreCaseTrimmed(@Param("channel") String channel, Pageable pageable);
}
