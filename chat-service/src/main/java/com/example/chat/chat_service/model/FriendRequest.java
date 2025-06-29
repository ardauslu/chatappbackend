package com.example.chat.chat_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * FriendRequest sınıfı, iki kullanıcı arasındaki arkadaşlık isteğini temsil eder.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Entity ve @Table: Bu sınıfın bir veritabanı tablosu olduğunu ve tablo adının "friend_requests" olduğunu belirtir.</li>
 *   <li>Her alan, bir arkadaşlık isteğinin hangi bilgileri içerdiğini gösterir (kimden, kime, durum, zaman).</li>
 *   <li>@Enumerated(EnumType.STRING): Enum değerinin veritabanında string olarak saklanmasını sağlar.</li>
 *   <li>get/set metotları, nesne alanlarına erişmek ve değer atamak için kullanılır.</li>
 * </ul>
 */
@Entity
@Table(name = "friend_requests")
public class FriendRequest {
    /**
     * Arkadaşlık isteğinin benzersiz kimliği.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * İsteği gönderen kullanıcının adı.
     */
    @Column(nullable = false)
    private String fromUsername;

    /**
     * İsteği alan kullanıcının adı.
     */
    @Column(nullable = false)
    private String toUsername;

    /**
     * İsteğin durumu (PENDING, ACCEPTED, REJECTED).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    /**
     * İsteğin oluşturulma zamanı.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Arkadaşlık isteği durumlarını temsil eden enum.
     */
    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
