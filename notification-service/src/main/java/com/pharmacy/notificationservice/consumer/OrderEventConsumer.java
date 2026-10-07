package com.pharmacy.notificationservice.consumer;

import com.pharmacy.notificationservice.event.OrderEvent;
import com.pharmacy.notificationservice.service.NotificationService;
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

    private final NotificationService notificationService;

    /**
     * GROUP-ID khác với inventory-service → nhận TẤT CẢ message của topic.
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

        log.info("┌─ [NOTIFICATION] Kafka message: partition={}, offset={}, key={}",
                partition, offset, key);
        log.info("│  Payload: {}", event);

        try {
            notificationService.handleOrderEvent(event);
        } catch (Exception e) {
            log.error("└─ Xử lý thất bại: orderId={}, lỗi={}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}