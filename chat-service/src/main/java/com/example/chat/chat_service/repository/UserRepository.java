package com.example.chat.chat_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.chat.chat_service.model.User;

/**
 * UserRepository arayüzü, User (kullanıcı) nesneleriyle ilgili veritabanı işlemlerini gerçekleştirmek için kullanılır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>JpaRepository, temel CRUD işlemlerini otomatik olarak sağlar.</li>
 *   <li>findByUsername: Kullanıcı adını tam eşleşmeyle arar.</li>
 *   <li>findByUsernameIgnoreCase: Kullanıcı adını büyük/küçük harf duyarsız arar.</li>
 * </ul>
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Kullanıcıyı tam kullanıcı adına göre bulur.
     * @param username Kullanıcı adı
     * @return User nesnesi (varsa)
     */
    Optional<User> findByUsername(String username);
    /**
     * Kullanıcıyı büyük/küçük harf duyarsız şekilde bulur.
     * @param username Kullanıcı adı
     * @return User nesnesi (varsa)
     */
    Optional<User> findByUsernameIgnoreCase(String username);
}
