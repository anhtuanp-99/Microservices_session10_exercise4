package com.pharmacy.pharmacyservice.config;

import com.pharmacy.pharmacyservice.event.OrderEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    /**
     * ProducerFactory được build từ KafkaProperties (đọc từ application.yml).
     * Ưu điểm: không cần hardcode lại các config serializer trong Java.
     */
    @Bean
    public ProducerFactory<String, OrderEvent> orderEventProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    /**
     * KafkaTemplate<String, OrderEvent> type-safe.
     * Key = String (medicineId dạng chuỗi).
     * Value = OrderEvent (auto serialize thành JSON).
     */
    @Bean
    public KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate(
            ProducerFactory<String, OrderEvent> orderEventProducerFactory) {
        return new KafkaTemplate<>(orderEventProducerFactory);
    }
}