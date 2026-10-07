package com.pharmacy.pharmacyservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event đại diện cho 1 dòng đơn hàng thuốc.
 * Được serialize JSON và gửi vào topic medicine-stock-events.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderEvent {

    private String orderId;             // Mã đơn hàng
    private Long medicineId;            // Dùng làm Kafka Message Key
    private String medicineName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String action;              // SALE, IMPORT, EXPORT

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}