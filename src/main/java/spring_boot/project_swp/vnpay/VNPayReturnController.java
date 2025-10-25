package spring_boot.project_swp.vnpay;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.service.PaymentService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;



@RestController
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/{vnpayreturn}")
    public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
        String vnp_SecureHash = allParams.get("vnp_SecureHash");

        if (allParams.containsKey("vnp_SecureHash")) {
            allParams.remove("vnp_SecureHash");
        }

        List<String> fieldNames = new ArrayList<>(allParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = allParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append('&');
                }
            }
            if (hashData.length() > 0) {
                hashData.deleteCharAt(hashData.length() - 1);
            }
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
        }

        String mySecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());

        System.out.println("====== VNPAY RETURN DEBUG ======");
        System.out.println("VNPAY Hash (từ URL): " + vnp_SecureHash);
        System.out.println("My Hash (tự tạo):   " + mySecureHash);
        System.out.println("Dữ liệu để hash:    " + hashData.toString());
        System.out.println("==================================");

        if (mySecureHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            String vnp_TxnRef = allParams.get("vnp_TxnRef"); // Payment ID
            String vnp_TransactionNo = allParams.get("vnp_TransactionNo"); // Transaction code
            String vnp_PayDate = allParams.get("vnp_PayDate"); // Transaction time (yyyyMMddHHmmss)

            if ("00".equals(vnp_ResponseCode)) {
                System.out.println("Thanh toán thành công cho đơn hàng: " + vnp_TxnRef);

                // Cập nhật payment trong database
                try {
                    int paymentId = Integer.parseInt(vnp_TxnRef);
                    Payment payment = paymentService.findPaymentById(paymentId)
                            .orElseThrow(() -> new RuntimeException("Payment not found"));

                    payment.setStatus("COMPLETED");
                    payment.setTransactionCode(vnp_TransactionNo);

                    // Convert vnp_PayDate từ format yyyyMMddHHmmss sang LocalDateTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    LocalDateTime transactionTime = LocalDateTime.parse(vnp_PayDate, formatter);
                    payment.setTransactionTime(transactionTime);

                    if (paymentService.UpdatePayment(payment) == false) {
                        throw new RuntimeException("Failed to update payment");
                    }

                    return ResponseEntity.ok("Giao dịch thành công!");
                } catch (Exception e) {
                    System.err.println("Lỗi cập nhật payment: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Lỗi cập nhật thông tin thanh toán");
                }
            } else {
                System.out.println("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);

                // Cập nhật status thành FAILED
                try {
                    int paymentId = Integer.parseInt(vnp_TxnRef);
                    Payment payment = paymentService.findById(paymentId)
                            .orElseThrow(() -> new RuntimeException("Payment not found"));

                    payment.setStatus("FAILED");
                    paymentService.savePayment(payment);
                } catch (Exception e) {
                    System.err.println("Lỗi cập nhật payment: " + e.getMessage());
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch thất bại!");
            }
        } else {
            System.out.println(">>> LỖI: Chữ ký không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chữ ký không hợp lệ!");
        }
    }


}