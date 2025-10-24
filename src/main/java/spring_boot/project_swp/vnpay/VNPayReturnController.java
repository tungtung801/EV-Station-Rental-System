package spring_boot.project_swp.vnpay;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;



@RestController
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @GetMapping("/vnpay_return")
    public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
        // Lấy vnp_SecureHash từ response nhưng không xóa khỏi map vội
        String vnp_SecureHash = allParams.get("vnp_SecureHash");

        // Xóa vnp_SecureHash khỏi map để chuẩn bị tạo lại chữ ký
        if (allParams.containsKey("vnp_SecureHash")) {
            allParams.remove("vnp_SecureHash");
        }

        // Sắp xếp các tham số và tạo lại chuỗi hash data
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
            // Xóa dấu '&' cuối cùng
            if (hashData.length() > 0) {
                hashData.deleteCharAt(hashData.length() - 1);
            }
        } catch (UnsupportedEncodingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
        }

        // Gọi hàm tạo chữ ký từ lớp tiện ích VNPayUtils
        String mySecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());

        // ================= DEBUG LOGGING (Quan trọng để kiểm tra) =================
        System.out.println("====== VNPAY RETURN DEBUG ======");
        System.out.println("VNPAY Hash (từ URL): " + vnp_SecureHash);
        System.out.println("My Hash (tự tạo):   " + mySecureHash);
        System.out.println("Dữ liệu để hash:    " + hashData.toString());
        System.out.println("==================================");

        if (mySecureHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            if ("00".equals(vnp_ResponseCode)) {
                System.out.println("Thanh toán thành công cho đơn hàng: " + allParams.get("vnp_TxnRef"));
                // TODO: Cập nhật trạng thái đơn hàng trong database của bạn
                return ResponseEntity.ok("Giao dịch thành công!");
            } else {
                System.out.println("Thanh toán thất bại. Mã lỗi: " + vnp_ResponseCode);
                // TODO: Cập nhật trạng thái đơn hàng trong database
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Giao dịch thất bại!");
            }
        } else {
            System.out.println(">>> LỖI: Chữ ký không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chữ ký không hợp lệ!");
        }
    }
}

