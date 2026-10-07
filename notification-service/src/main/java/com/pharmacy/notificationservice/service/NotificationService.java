package com.pharmacy.notificationservice.service;

import com.pharmacy.notificationservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    /**
     * Xử lý event: gửi thông báo hóa đơn cho khách hàng.
     * Không cần transaction vì không đụng DB.
     */
    public void handleOrderEvent(OrderEvent event) {
        log.info("Hóa đơn cho đơn hàng [{}] đã được gửi tới khách hàng",
                event.getOrderId());

        // Gửi email (log-only hoặc thật tùy config)
        emailService.sendInvoiceEmail(event);
    }
}