package com.example.chat.chat_service.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * User sınıfı, uygulamadaki bir kullanıcıyı temsil eder.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Entity ve @Table: Bu sınıfın bir veritabanı tablosu olduğunu ve tablo adının "users" olduğunu belirtir.</li>
 *   <li>Her alan, bir kullanıcının hangi bilgileri içerdiğini gösterir (id, kullanıcı adı, arkadaşlar).</li>
 *   <li>@ManyToMany: Kullanıcılar arasında çoklu arkadaşlık ilişkisini belirtir.</li>
 *   <li>get/set metotları, nesne alanlarına erişmek ve değer atamak için kullanılır.</li>
 * </ul>
 */
@Entity
@Table(name = "users")
public class User {
    /**
     * Kullanıcının benzersiz kimliği.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Kullanıcının benzersiz kullanıcı adı.
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Kullanıcının arkadaşları (accepted ilişkiler).
     * <p>
     * <b>Nasıl çalışır?</b> @ManyToMany ile kullanıcılar arasında çift yönlü arkadaşlık ilişkisi kurulur.
     * user_friends tablosu ile iki kullanıcı birbirine bağlanır.
     * </p>
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_friends",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<User> friends = new HashSet<>();

    // ...getters/setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Set<User> getFriends() { return friends; }
    public void setFriends(Set<User> friends) { this.friends = friends; }
}
