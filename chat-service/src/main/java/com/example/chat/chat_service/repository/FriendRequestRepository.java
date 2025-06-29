package com.example.chat.chat_service.repository;

import com.example.chat.chat_service.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * FriendRequestRepository arayüzü, FriendRequest (arkadaşlık isteği) nesneleriyle ilgili veritabanı işlemlerini gerçekleştirir.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>JpaRepository, temel CRUD işlemlerini otomatik olarak sağlar.</li>
 *   <li>Özel metotlar, belirli kullanıcılar ve durumlar için sorgu yapmayı kolaylaştırır.</li>
 * </ul>
 */
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    /**
     * Belirli bir kullanıcının belirli durumdaki gönderdiği istekleri bulur.
     */
    List<FriendRequest> findByFromUsernameAndStatus(String fromUsername, FriendRequest.Status status);
    /**
     * Belirli bir kullanıcıya gelen belirli durumdaki istekleri bulur.
     */
    List<FriendRequest> findByToUsernameAndStatus(String toUsername, FriendRequest.Status status);
    /**
     * Belirli bir kullanıcıya ait tüm istekleri bulur.
     */
    List<FriendRequest> findByFromUsernameOrToUsername(String fromUsername, String toUsername);
}
