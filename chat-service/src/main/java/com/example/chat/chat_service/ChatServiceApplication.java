package com.example.chat.chat_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ChatServiceApplication, Spring Boot chat uygulamasının ana giriş noktasıdır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@SpringBootApplication: Spring Boot'un otomatik yapılandırmasını ve bileşen taramasını başlatır.</li>
 *   <li>main metodu: Java uygulamalarında ana giriş noktasıdır, uygulamayı başlatır.</li>
 *   <li>SpringApplication.run: Spring Boot uygulamasını başlatır.</li>
 * </ul>
 */
@SpringBootApplication // Spring Boot'un otomatik yapılandırmasını ve bileşen taramasını başlatır.
public class ChatServiceApplication {
    /**
     * Uygulamanın ana giriş noktası.
     * <p>
     * <b>Nasıl çalışır?</b> Spring Boot uygulamasını başlatır.
     * </p>
     * @param args Komut satırı argümanları
     */
    public static void main(String[] args) {
        // Spring Boot uygulamasını başlatır.
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
