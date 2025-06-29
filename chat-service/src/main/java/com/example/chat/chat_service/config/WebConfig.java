package com.example.chat.chat_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig, uygulamanın CORS (Cross-Origin Resource Sharing) ayarlarını yapılandırır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Configuration: Bu sınıfın bir konfigürasyon sınıfı olduğunu belirtir.</li>
 *   <li>corsConfigurer: Frontend (React) ile backend (Spring Boot) arasında güvenli iletişim için CORS ayarlarını yapar.</li>
 *   <li>addCorsMappings: Hangi adreslerden, hangi metotlarla ve hangi izinlerle erişim olacağını belirler.</li>
 * </ul>
 */
@Configuration
public class WebConfig {
    /**
     * CORS ayarlarını yapılandıran bean.
     * <p>
     * <b>Nasıl çalışır?</b> Tüm endpoint'lere http://localhost:3000 adresinden erişime izin verir.
     * </p>
     * @return WebMvcConfigurer nesnesi
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
