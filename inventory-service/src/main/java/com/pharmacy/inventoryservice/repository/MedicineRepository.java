package com.pharmacy.inventoryservice.repository;

import com.pharmacy.inventoryservice.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /**
     * Trừ stock — dùng UPDATE có điều kiện để tránh race condition.
     * Trả về số dòng bị update:
     *   - 1: thành công
     *   - 0: không tìm thấy thuốc HOẶC stock không đủ
     *
     * clearAutomatically = true: xóa cache persistence context sau update
     *   → tránh Hibernate trả về stale data ở query tiếp theo
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Medicine m SET m.stockQuantity = m.stockQuantity - :quantity " +
            "WHERE m.id = :id AND m.stockQuantity >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}