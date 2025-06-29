package com.example.chat.chat_service.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.example.chat.chat_service.model.Conversation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ChatMessage sınıfı, bir sohbet mesajını temsil eder ve veritabanında saklanır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Entity ve @Table: Bu sınıfın bir veritabanı tablosu olduğunu ve tablo adının "chat_messages" olduğunu belirtir.</li>
 *   <li>Serializable: Nesnenin ağ üzerinden veya dosyaya yazılarak taşınabilmesini sağlar.</li>
 *   <li>Her alan (field), bir mesajın hangi bilgileri içerdiğini gösterir (gönderen, içerik, zaman, kanal, sohbet ilişkisi).</li>
 *   <li>@Id ve @GeneratedValue: id alanı, her mesaj için otomatik olarak artan birincil anahtardır.</li>
 *   <li>@ManyToOne ve @JoinColumn: Her mesaj bir sohbete (Conversation) aittir. Bu ilişkiyi veritabanında "conversation_id" sütunu ile kurarız.</li>
 *   <li>get/set metotları, nesne alanlarına erişmek ve değer atamak için kullanılır.</li>
 *   <li>Statik yardımcı metotlar, özel kanal isimlendirme ve erişim kontrolü gibi işlevler sağlar.</li>
 * </ul>
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage implements Serializable {
    /**
     * Mesajın benzersiz kimliği. Veritabanı tarafından otomatik atanır.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mesajı gönderen kullanıcının adı.
     */
    @Column(nullable = false)
    private String sender;

    /**
     * Mesajın metin içeriği.
     */
    @Column(nullable = false)
    private String content;

    /**
     * Mesajın gönderildiği zaman (tarih ve saat).
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Mesajın ait olduğu kanal adı (genel veya özel kanal ismi).
     */
    @Column(nullable = true)
    private String channel;

    /**
     * Mesajın ait olduğu Conversation (sohbet) nesnesi.
     * @ManyToOne: Bir sohbetin birden fazla mesajı olabilir.
     * @JoinColumn: Veritabanında "conversation_id" sütunu ile ilişkilendirilir.
     */
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    /**
     * İki kullanıcıdan oluşan bir private kanal adı üretici yardımcı fonksiyon.
     * <p>
     * <b>Nasıl çalışır?</b> Kullanıcı adlarını alfabetik olarak sıralar ve "private_kullanici1_kullanici2" formatında kanal adı oluşturur.
     * Bu sayede iki kullanıcı arasındaki özel kanal ismi her zaman aynı olur.
     * </p>
     * @param user1 Birinci kullanıcı adı
     * @param user2 İkinci kullanıcı adı
     * @return Özel kanal adı (ör: private_ali_ayse)
     */
    public static String getPrivateChannelName(String user1, String user2) {
        List<String> users = Arrays.asList(user1, user2);
        users.sort(String::compareToIgnoreCase); // Alfabetik sıralama
        return "private_" + users.get(0) + "_" + users.get(1);
    }

    /**
     * Bir kullanıcının, bir private kanala erişim hakkı olup olmadığını kontrol eder.
     * <p>
     * <b>Nasıl çalışır?</b> Kanal adı "private_kullanici1_kullanici2" formatında olmalı.
     * Fonksiyon, kanal adını parçalara ayırır ve kullanıcının bu iki isimden biri olup olmadığını kontrol eder.
     * </p>
     * @param username Kullanıcı adı
     * @param channel Kanal adı
     * @return Kullanıcı erişebiliyorsa true, aksi halde false
     */
    public static boolean canAccessPrivateChannel(String username, String channel) {
        if (channel == null || !channel.startsWith("private_")) return false;
        String[] parts = channel.substring(8).split("_");
        if (parts.length != 2) return false;
        return username.equalsIgnoreCase(parts[0]) || username.equalsIgnoreCase(parts[1]);
    }

    // Getters and setters
    /**
     * Mesajın kimliğini döndürür.
     */
    public Long getId() { return id; }
    /**
     * Mesajın kimliğini ayarlar.
     */
    public void setId(Long id) { this.id = id; }
    /**
     * Mesajı gönderen kullanıcıyı döndürür.
     */
    public String getSender() { return sender; }
    /**
     * Mesajı gönderen kullanıcıyı ayarlar.
     */
    public void setSender(String sender) { this.sender = sender; }
    /**
     * Mesaj içeriğini döndürür.
     */
    public String getContent() { return content; }
    /**
     * Mesaj içeriğini ayarlar.
     */
    public void setContent(String content) { this.content = content; }
    /**
     * Mesajın gönderilme zamanını döndürür.
     */
    public LocalDateTime getTimestamp() { return timestamp; }
    /**
     * Mesajın gönderilme zamanını ayarlar.
     */
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    /**
     * Mesajın kanal adını döndürür.
     */
    public String getChannel() { return channel; }
    /**
     * Mesajın kanal adını ayarlar.
     */
    public void setChannel(String channel) { this.channel = channel; }
    /**
     * Mesajın ait olduğu Conversation nesnesini döndürür.
     */
    public Conversation getConversation() { return conversation; }
    /**
     * Mesajın ait olduğu Conversation nesnesini ayarlar.
     */
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
}
