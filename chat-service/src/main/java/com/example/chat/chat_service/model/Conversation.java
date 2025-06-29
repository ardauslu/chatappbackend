package com.example.chat.chat_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Conversation sınıfı, iki kullanıcı arasındaki birebir sohbeti temsil eder.
 *
 * <p>Yeni başlayanlar için açıklama:</p>
 * <ul>
 *   <li>@Entity: Bu sınıfın bir veritabanı tablosu olduğunu belirtir.</li>
 *   <li>@Table(name = "conversations"): Bu entity'nin veritabanında "conversations" adlı tabloya karşılık geldiğini belirtir.</li>
 *   <li>id: Her sohbetin benzersiz kimliği (otomatik artan).</li>
 *   <li>user1, user2: Sohbete katılan iki kullanıcının adları.</li>
 *   <li>createdAt: Sohbetin oluşturulma zamanı.</li>
 *   <li>get/set metotları: Alanlara erişmek ve değer atamak için kullanılır.</li>
 * </ul>
 */
@Entity
@Table(name = "conversations")
public class Conversation {
    /**
     * Sohbetin benzersiz kimliği. Otomatik olarak veritabanı tarafından atanır.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Sohbete katılan birinci kullanıcının adı.
     */
    @Column(nullable = false)
    private String user1;

    /**
     * Sohbete katılan ikinci kullanıcının adı.
     */
    @Column(nullable = false)
    private String user2;

    /**
     * Sohbetin oluşturulma zamanı. Varsayılan olarak şu anki zaman atanır.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getter ve setter metotları
    /**
     * Sohbetin kimliğini döndürür.
     * @return id
     */
    public Long getId() { return id; }
    /**
     * Sohbetin kimliğini ayarlar.
     * @param id Sohbet kimliği
     */
    public void setId(Long id) { this.id = id; }
    /**
     * Birinci kullanıcının adını döndürür.
     * @return user1
     */
    public String getUser1() { return user1; }
    /**
     * Birinci kullanıcının adını ayarlar.
     * @param user1 Kullanıcı adı
     */
    public void setUser1(String user1) { this.user1 = user1; }
    /**
     * İkinci kullanıcının adını döndürür.
     * @return user2
     */
    public String getUser2() { return user2; }
    /**
     * İkinci kullanıcının adını ayarlar.
     * @param user2 Kullanıcı adı
     */
    public void setUser2(String user2) { this.user2 = user2; }
    /**
     * Sohbetin oluşturulma zamanını döndürür.
     * @return createdAt
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
    /**
     * Sohbetin oluşturulma zamanını ayarlar.
     * @param createdAt Oluşturulma zamanı
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
