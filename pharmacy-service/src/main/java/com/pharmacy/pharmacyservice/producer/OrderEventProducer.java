package com.pharmacy.pharmacyservice.producer;

import com.pharmacy.pharmacyservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;

    @Value("${app.kafka.topic.stock-events}")
    private String stockEventsTopic;

    /**
     * Gửi OrderEvent vào Kafka, CHỜ ACK tối đa 3s.
     * Key = medicineId → đảm bảo cùng thuốc vào cùng partition.
     */
    public SendResult<String, OrderEvent> send(OrderEvent event) {
        String key = String.valueOf(event.getMedicineId());

        log.info("Gửi event: topic={}, key={}, orderId={}, medicineId={}, quantity={}",
                stockEventsTopic, key, event.getOrderId(),
                event.getMedicineId(), event.getQuantity());

        try {
            CompletableFuture<SendResult<String, OrderEvent>> future =
                    orderEventKafkaTemplate.send(stockEventsTopic, key, event);

            SendResult<String, OrderEvent> result = future.get(3, TimeUnit.SECONDS);

            log.info("<<< Gửi thành công: partition={}, offset={}",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

            return result;

        } catch (Exception e) {
            log.error(">>> Gửi thất bại: orderId={}, lỗi={}",
                    event.getOrderId(), e.getMessage(), e);
            throw new RuntimeException(
                    "Không thể gửi sự kiện đơn hàng lên Kafka: " + e.getMessage(), e);
        }
    }
}