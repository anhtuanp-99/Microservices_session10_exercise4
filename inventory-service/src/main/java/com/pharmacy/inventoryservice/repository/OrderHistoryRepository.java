package com.pharmacy.inventoryservice.repository;

import com.pharmacy.inventoryservice.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    /**
     * Chống xử lý trùng: check xem event (orderId, medicineId) đã được xử lý chưa.
     */
    boolean existsByOrderIdAndMedicineId(String orderId, Long medicineId);
}