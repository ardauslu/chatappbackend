package com.example.chat.chat_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.chat_service.model.Conversation;

/**
 * ConversationRepository arayüzü, Conversation (sohbet) nesneleriyle ilgili
 * veritabanı işlemlerini gerçekleştirmek için Spring Data JPA tarafından kullanılır.
 * Bu arayüz, iki kullanıcı arasındaki sohbeti bulmak için özel sorgu metodları içerir.
 *
 * <p>Yeni başlayanlar için açıklama:</p>
 * <ul>
 *   <li>JpaRepository, temel CRUD (oluşturma, okuma, güncelleme, silme) işlemlerini otomatik olarak sağlar.</li>
 *   <li>Buradaki metotlar, kullanıcılar arasındaki birebir sohbeti bulmak için kullanılır.</li>
 *   <li>findByUser1AndUser2: user1 ve user2 isimli kullanıcılar arasındaki sohbeti bulur.</li>
 *   <li>findByUser2AndUser1: user2 ve user1 isimli kullanıcılar arasındaki sohbeti bulur (ters kombinasyon için).</li>
 *   <li>Bu iki metot, kullanıcıların sırası farklı olduğunda da sohbetin bulunmasını sağlar.</li>
 * </ul>
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    /**
     * Belirtilen iki kullanıcı arasındaki sohbeti bulur.
     *
     * @param user1 Birinci kullanıcının adı
     * @param user2 İkinci kullanıcının adı
     * @return İki kullanıcı arasındaki Conversation nesnesi (varsa)
     */
    Optional<Conversation> findByUser1AndUser2(String user1, String user2);

    /**
     * Belirtilen iki kullanıcı arasındaki sohbeti (kullanıcı sırası ters) bulur.
     *
     * @param user2 İkinci kullanıcının adı
     * @param user1 Birinci kullanıcının adı
     * @return İki kullanıcı arasındaki Conversation nesnesi (varsa)
     */
    Optional<Conversation> findByUser2AndUser1(String user2, String user1);
}
