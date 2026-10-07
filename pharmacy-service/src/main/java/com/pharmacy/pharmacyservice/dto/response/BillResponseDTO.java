package com.pharmacy.pharmacyservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponseDTO {

    private String orderId;                  // ← THÊM
    private List<BillLineDTO> lines;

    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String currency;
    private String message;                  // ← THÊM

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillLineDTO {
        private Long medicineId;
        private String medicineName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private String stockSource;
        private String stockMessage;
    }
}