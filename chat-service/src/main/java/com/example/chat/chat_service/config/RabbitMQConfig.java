package com.example.chat.chat_service.config;

/**
 * RabbitMQConfig, uygulamanın RabbitMQ mesajlaşma altyapısını yapılandırır.
 * <p>
 * <b>Java öğrenenler için açıklama:</b>
 * <ul>
 *   <li>@Configuration: Bu sınıfın bir konfigürasyon sınıfı olduğunu belirtir.</li>
 *   <li>Queue, Exchange, Binding: RabbitMQ'da mesajların nasıl yönlendirileceğini belirler.</li>
 *   <li>Jackson2JsonMessageConverter: Mesajların JSON formatında iletilmesini sağlar.</li>
 *   <li>RabbitTemplate: Mesaj göndermek için kullanılır.</li>
 * </ul>
 */
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE = "chat.queue";
    public static final String EXCHANGE = "chat.exchange";
    public static final String ROUTING_KEY = "chat.message";

    /**
     * RabbitMQ'da kalıcı (durable) bir kuyruk oluşturur.
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true); // Kuyruk kalıcı
    }

    /**
     * RabbitMQ'da bir exchange oluşturur.
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * Kuyruk ile exchange arasında bağlantı (binding) kurar.
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * Mesajları JSON formatında iletmek için converter bean'i.
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate bean'i, mesaj göndermek için kullanılır.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(EXCHANGE);
        template.setRoutingKey(ROUTING_KEY);
        return template;
    }
}
