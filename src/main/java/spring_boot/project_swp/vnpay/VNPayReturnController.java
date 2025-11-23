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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.entity.Payment; // Import Payment Entity
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.repository.PaymentRepository; // Import Repository
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "VNPay APIs")
@Slf4j
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    final PaymentService paymentService;

    // [NEW] Thêm Repository để lấy thông tin xe và booking
    final PaymentRepository paymentRepository;

    @GetMapping("/vnpay_return")
    public void handleVNPayReturn(
            @RequestParam Map<String, String> allParams,
            HttpServletResponse response) throws IOException {

        String vnp_SecureHash = allParams.get("vnp_SecureHash");
        if (allParams.containsKey("vnp_SecureHashType")) {
            allParams.remove("vnp_SecureHashType");
        }
        allParams.remove("vnp_SecureHash");

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
        String frontendUrl = "http://localhost:5173/payment-result";

        if (calculatedHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            String vnp_TxnRef = allParams.get("vnp_TxnRef");

            // Lấy Payment ID
            Long paymentId = Long.parseLong(vnp_TxnRef.split("_")[1]);

            // [NEW] Lấy thông tin Payment từ DB để biết bookingId và carId
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            Long bookingId = null;
            Long carId = null;

            if (payment != null && payment.getBooking() != null) {
                bookingId = payment.getBooking().getBookingId();
                // Lấy ID xe để Frontend biết đường quay về
                carId = payment.getBooking().getVehicle().getVehicleId();
            }

            if ("00".equals(vnp_ResponseCode)) {
                log.info("VNPay Success: PaymentId={}", paymentId);

                PaymentStatusUpdateRequest updateReq = new PaymentStatusUpdateRequest();
                updateReq.setStatus(PaymentStatusEnum.SUCCESS);

                // Lấy mã giao dịch VNPay từ response
                String transactionCode = allParams.get("vnp_TransactionNo");
                if (transactionCode != null && !transactionCode.isEmpty()) {
                    updateReq.setTransactionCode(transactionCode);
                    log.info("VNPay Transaction Code: {}", transactionCode);
                }

                try {
                    paymentService.updatePaymentStatus(paymentId, updateReq);

                    // [NEW] Gửi kèm carId và bookingId về Frontend
                    String redirectUrl = frontendUrl + "?status=success"
                            + "&carId=" + (carId != null ? carId : "")
                            + "&bookingId=" + (bookingId != null ? bookingId : "");

                    response.sendRedirect(redirectUrl);
                } catch (Exception e) {
                    log.error("Error updating payment status: {}", e.getMessage());
                    response.sendRedirect(frontendUrl + "?status=error");
                }

            } else {
                log.warn("VNPay Failed: Code={}, PaymentId={}", vnp_ResponseCode, paymentId);

                PaymentStatusUpdateRequest updateReq = new PaymentStatusUpdateRequest();
                updateReq.setStatus(PaymentStatusEnum.FAILED);
                paymentService.updatePaymentStatus(paymentId, updateReq);

                // [NEW] Cũng gửi kèm ID để quay về trang xe dù lỗi
                String redirectUrl = frontendUrl + "?status=failed"
                        + "&carId=" + (carId != null ? carId : "");

                response.sendRedirect(redirectUrl);
            }
        } else {
            log.error("VNPay Checksum Invalid!");
            response.sendRedirect(frontendUrl + "?status=invalid_signature");
        }
    }
}