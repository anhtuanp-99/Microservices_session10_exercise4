package com.pharmacy.pharmacyservice.controller;

import com.pharmacy.pharmacyservice.dto.request.BillRequestDTO;
import com.pharmacy.pharmacyservice.dto.response.BillResponseDTO;
import com.pharmacy.pharmacyservice.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponseDTO> calculateBill(
            @Valid @RequestBody BillRequestDTO request) {
        return ResponseEntity.ok(billService.processBill(request));
    }
}