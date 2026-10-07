package com.pharmacy.pharmacyservice.service;

import com.pharmacy.pharmacyservice.dto.request.BillItemDTO;
import com.pharmacy.pharmacyservice.dto.request.BillRequestDTO;
import com.pharmacy.pharmacyservice.dto.response.BillResponseDTO;
import com.pharmacy.pharmacyservice.entity.Medicine;
import com.pharmacy.pharmacyservice.event.OrderEvent;
import com.pharmacy.pharmacyservice.exception.ResourceNotFoundException;
import com.pharmacy.pharmacyservice.producer.OrderEventProducer;
import com.pharmacy.pharmacyservice.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.05");

    private final MedicineRepository medicineRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * Xử lý thanh toán hóa đơn:
     *   1. Validate + tính tiền từng dòng
     *   2. Gửi Kafka event cho từng dòng
     *   3. Tính tổng + VAT
     *   4. Trả response
     *
     * Tối ưu: gộp tính tiền + gửi event trong 1 vòng lặp → N query thay vì 2N.
     */
    public BillResponseDTO processBill(BillRequestDTO request) {
        String orderId = "ORD-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        List<BillResponseDTO.BillLineDTO> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // ===== GỘP VÒNG LẶP: tính tiền + gửi event =====
        for (BillItemDTO item : request.getItems()) {
            Medicine medicine = medicineRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medicine", "ID", item.getMedicineId()));

            BigDecimal lineTotal = medicine.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            // 1. Thêm dòng hóa đơn
            lines.add(BillResponseDTO.BillLineDTO.builder()
                    .medicineId(medicine.getId())
                    .medicineName(medicine.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(medicine.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build());

            subtotal = subtotal.add(lineTotal);

            // 2. Gửi event Kafka — KHÔNG cần query DB lần 2
            OrderEvent event = OrderEvent.builder()
                    .orderId(orderId)
                    .medicineId(medicine.getId())
                    .medicineName(medicine.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(medicine.getUnitPrice())
                    .totalPrice(lineTotal)
                    .action("SALE")
                    .timestamp(now)
                    .build();

            SendResult<String, OrderEvent> result = orderEventProducer.send(event);
            log.info("Event {} (medicineId={}) → partition {}, offset {}",
                    orderId, medicine.getId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        }

        // ===== TÍNH VAT SAU KHI ĐÃ GỬI EVENT =====
        BigDecimal vatAmount = subtotal.multiply(VAT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(vatAmount);

        log.info("Hóa đơn {} — subtotal={}, VAT={}, total={}",
                orderId, subtotal, vatAmount, totalAmount);

        return BillResponseDTO.builder()
                .orderId(orderId)
                .lines(lines)
                .subtotal(subtotal)
                .vatRate(VAT_RATE)
                .vatAmount(vatAmount)
                .totalAmount(totalAmount)
                .currency("VND")
                .message("Thanh toán thành công, sự kiện đã gửi tới Kafka")
                .build();
    }
}