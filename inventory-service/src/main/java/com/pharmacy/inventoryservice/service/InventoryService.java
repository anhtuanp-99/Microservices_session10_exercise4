package com.pharmacy.inventoryservice.service;

import com.pharmacy.inventoryservice.entity.Medicine;
import com.pharmacy.inventoryservice.entity.OrderHistory;
import com.pharmacy.inventoryservice.event.OrderEvent;
import com.pharmacy.inventoryservice.repository.MedicineRepository;
import com.pharmacy.inventoryservice.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    /**
     * Xử lý OrderEvent theo kiểu IDEMPOTENT:
     *   1. Kiểm tra event đã xử lý chưa (idempotency).
     *   2. Trừ stock (chỉ khi đủ hàng).
     *   3. Lưu lịch sử đơn hàng.
     * Cả 3 bước trong 1 TRANSACTION → đảm bảo atomic.
     */
    @Transactional
    public void processOrderEvent(OrderEvent event) {
        log.info(">>> Nhận event: orderId={}, medicineId={}, quantity={}",
                event.getOrderId(), event.getMedicineId(), event.getQuantity());

        // ===== BƯỚC 1: Idempotency — chống xử lý trùng =====
        boolean alreadyProcessed = orderHistoryRepository
                .existsByOrderIdAndMedicineId(event.getOrderId(), event.getMedicineId());
        if (alreadyProcessed) {
            log.warn("<<< Event đã xử lý trước đó, SKIP: orderId={}, medicineId={}",
                    event.getOrderId(), event.getMedicineId());
            return;
        }

        // ===== BƯỚC 2: Trừ stock =====
        int updated = medicineRepository.decreaseStock(
                event.getMedicineId(), event.getQuantity());

        if (updated == 0) {
            log.error("<<< Không thể trừ stock: medicineId={} — " +
                            "thuốc không tồn tại hoặc không đủ hàng",
                    event.getMedicineId());
            // Có thể throw → Kafka retry. Nhưng ở bài này log và return.
            // Trong production: throw để retry hoặc đẩy vào Dead Letter Topic.
            return;
        }

        // Lấy stock mới để log
        Medicine medicine = medicineRepository.findById(event.getMedicineId()).orElse(null);
        Integer newStock = medicine != null ? medicine.getStockQuantity() : null;

        log.info("<<< Trừ stock thành công: medicineId={}, quantity={}, stock mới={}",
                event.getMedicineId(), event.getQuantity(), newStock);

        // ===== BƯỚC 3: Lưu lịch sử đơn hàng =====
        OrderHistory history = OrderHistory.builder()
                .orderId(event.getOrderId())
                .medicineId(event.getMedicineId())
                .medicineName(event.getMedicineName())
                .quantity(event.getQuantity())
                .unitPrice(event.getUnitPrice())
                .totalPrice(event.getTotalPrice())
                .action(event.getAction())
                .eventTimestamp(event.getTimestamp())
                .processedAt(LocalDateTime.now())
                .build();

        orderHistoryRepository.save(history);
        log.info("<<< Đã lưu lịch sử đơn hàng: id={}", history.getId());
    }
}