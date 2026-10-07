package com.pharmacy.inventoryservice.consumer;

import com.pharmacy.inventoryservice.event.OrderEvent;
import com.pharmacy.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    /**
     * Lắng nghe topic medicine-stock-events.
     * <p>
     * Đọc key (medicineId) + value (OrderEvent) + partition + offset để log.
     * <p>
     * Nếu InventoryService.processOrderEvent throw exception:
     *   → Spring Kafka sẽ KHÔNG commit offset → message sẽ được đọc lại.
     *   → Đây là cơ chế "at-least-once" (đảm bảo ít nhất 1 lần xử lý).
     */
    @KafkaListener(
            topics = "${app.kafka.topic.stock-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void onOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key) {

        log.info("┌─ Kafka message: partition={}, offset={}, key={}", partition, offset, key);
        log.info("│  Payload: {}", event);

        try {
            inventoryService.processOrderEvent(event);
            log.info("└─ Xử lý thành công: orderId={}, medicineId={}",
                    event.getOrderId(), event.getMedicineId());
        } catch (Exception e) {
            log.error("└─ Xử lý thất bại: orderId={}, lỗi={}",
                    event.getOrderId(), e.getMessage(), e);
            // Không catch để Kafka retry: re-throw
            throw e;
        }
    }
}