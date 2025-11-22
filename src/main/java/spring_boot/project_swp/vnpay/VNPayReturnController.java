package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "VNPay APIs")
@Slf4j
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    // CHỈ CẦN GỌI PAYMENT SERVICE LÀ ĐỦ
    // BookingService và Rental logic đã nằm trong PaymentService rồi
    final PaymentService paymentService;

    @GetMapping("/vnpay_return")
    public void handleVNPayReturn(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        // 1. Lấy và Validate Checksum (Chữ ký bảo mật)
        String vnp_SecureHash = allParams.get("vnp_SecureHash");
        if (allParams.containsKey("vnp_SecureHashType")) {
            allParams.remove("vnp_SecureHashType");
        }
        allParams.remove("vnp_SecureHash");

        // Sắp xếp tham số
        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = allParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (Exception e) {
                    log.error("Encoding error: {}", e.getMessage());
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = VnpayUtils.hmacSHA512(hashSecret, hashData.toString());

        // URL Frontend để redirect về sau khi thanh toán
        // Ví dụ: http://localhost:5173/payment-result
        String frontendUrl = "http://localhost:5173/payment-result";

        if (calculatedHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            String vnp_TxnRef = allParams.get("vnp_TxnRef"); // Định dạng: PAY_{paymentId}_{timestamp}

            // Lấy Payment ID từ TxnRef
            Long paymentId = Long.parseLong(vnp_TxnRef.split("_")[1]);

            if ("00".equals(vnp_ResponseCode)) {
                // --- GIAO DỊCH THÀNH CÔNG ---
                log.info("VNPay Success: PaymentId={}", paymentId);

                // Gọi Service để update Payment -> Tự động kích hoạt Booking & Rental
                PaymentStatusUpdateRequest updateReq = new PaymentStatusUpdateRequest();
                updateReq.setStatus(PaymentStatusEnum.SUCCESS);

                try {
                    paymentService.updatePaymentStatus(paymentId, updateReq);
                    // Redirect về Frontend báo thành công
                    response.sendRedirect(frontendUrl + "?status=success&code=" + vnp_TxnRef);
                } catch (Exception e) {
                    log.error("Error updating payment status: {}", e.getMessage());
                    response.sendRedirect(frontendUrl + "?status=error");
                }

            } else {
                // --- GIAO DỊCH THẤT BẠI ---
                log.warn("VNPay Failed: Code={}, PaymentId={}", vnp_ResponseCode, paymentId);

                // Có thể update payment thành FAILED nếu muốn
                PaymentStatusUpdateRequest updateReq = new PaymentStatusUpdateRequest();
                updateReq.setStatus(PaymentStatusEnum.FAILED);
                paymentService.updatePaymentStatus(paymentId, updateReq);

                response.sendRedirect(frontendUrl + "?status=failed");
            }
        } else {
            // --- SAI CHỮ KÝ (CÓ DẤU HIỆU GIAN LẬN) ---
            log.error("VNPay Checksum Invalid!");
            response.sendRedirect(frontendUrl + "?status=invalid_signature");
        }
    }
}