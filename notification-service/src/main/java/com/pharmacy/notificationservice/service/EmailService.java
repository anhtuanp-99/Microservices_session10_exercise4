package com.pharmacy.notificationservice.service;

import com.pharmacy.notificationservice.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.notification.customer-email}")
    private String defaultCustomerEmail;

    public void sendInvoiceEmail(OrderEvent event) {
        // Ưu tiên lấy email người nhận từ OrderEvent (nếu có), nếu không thì dùng email mặc định trong yml
        String targetEmail = defaultCustomerEmail;

        if (!emailEnabled) {
            log.info("[EMAIL DISABLED] Sẽ gửi tới {}: Hóa đơn {} — {} × {} = {} VND",
                    targetEmail, event.getOrderId(),
                    event.getMedicineName(), event.getQuantity(), event.getTotalPrice());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(targetEmail);
            message.setSubject("[Nhà thuốc] Hóa đơn " + event.getOrderId());
            message.setText(buildContent(event));

            mailSender.send(message);
            log.info(">>> Đã gửi email hóa đơn {} từ [{}] tới [{}]", event.getOrderId(), senderEmail, targetEmail);

        } catch (Exception e) {
            log.error(">>> Gửi email thất bại: orderId={}, lỗi={}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    private String buildContent(OrderEvent event) {
        return """
                Nhà Thuốc Số 1
                ================================
                Xin chào quý khách,
                Cảm ơn quý khách đã mua hàng.

                Mã đơn hàng : %s
                Thuốc       : %s
                Số lượng    : %d
                Đơn giá     : %s VND
                Tổng tiền   : %s VND
                Thời gian   : %s

                Hotline: 1900xxxx
                Địa chỉ: 123 Lê Lợi, Q1, TP.HCM
                """.formatted(
                event.getOrderId(),
                event.getMedicineName(),
                event.getQuantity(),
                event.getUnitPrice().toPlainString(),
                event.getTotalPrice().toPlainString(),
                event.getTimestamp()
        );
    }
}