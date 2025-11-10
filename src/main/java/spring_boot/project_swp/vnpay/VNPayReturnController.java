package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.UserService;

@Tag(name = "VNPay APIs", description = "Các API liên quan đến xử lý thanh toán VNPay")
@RestController
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Autowired private PaymentService paymentService;
    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;
    @Autowired private RentalService rentalService;

    @Operation(
            summary = "Xử lý phản hồi trả về từ VNPay",
            description =
                    "Kiểm tra chữ ký, xác nhận trạng thái thanh toán và cập nhật thông tin thanh toán trong cơ sở dữ liệu")
    @GetMapping("/vnpay_return")
    public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
        String vnp_SecureHash = allParams.get("vnp_SecureHash");
        allParams.remove("vnp_SecureHash");

        // Sắp xếp và tạo chuỗi hash
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

        if (mySecureHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            String vnp_TxnRef = allParams.get("vnp_TxnRef");

            if ("00".equals(vnp_ResponseCode)) {
                try {

                    System.out.println("vnp_TxnRef:" + vnp_TxnRef);
                    // 1. Tìm payment bằng transaction code (vnp_TxnRef)
                    PaymentResponse paymentResponse = paymentService.findPaymentByTransactionCode(vnp_TxnRef);
                    System.out.println("paymentResponse: " + paymentResponse);

                    // 2. Cập nhật trạng thái payment thành SUCCESS
                    // Lưu ý: paymentService.updatePaymentStatus() sẽ tự động xử lý:
                    // - Cập nhật booking status thành DEPOSIT_PAID
                    // - Tạo rental từ booking
                    paymentService.updatePaymentStatus(
                            paymentResponse.getPaymentId(), PaymentStatusEnum.SUCCESS);


                    // 3. Lấy rental ID từ booking
                    Long bookingId = paymentResponse.getBookingId();
                    Long rentalId = null;

                    if (bookingId != null) {
                        // Tìm rental được tạo từ booking này
                        // Cách tốt hơn: lấy tất cả rentals và tìm cái match với bookingId
                        var allRentals = rentalService.getAllRentals();
                        var rentalOptional =
                                allRentals.stream()
                                        .filter(
                                                rental ->
                                                        rental.getBookingId() != null
                                                                && rental.getBookingId().equals(bookingId))
                                        .findFirst();
                        if (rentalOptional.isPresent()) {
                            rentalId = rentalOptional.get().getRentalId();
                            System.out.println("Found rentalId: " + rentalId + " for bookingId: " + bookingId);
                        } else {
                            System.out.println("No rental found for bookingId: " + bookingId);
                        }
                    }

                    // 4. Trả về response với rental ID
                    String responseMessage = "Giao dịch thành công! Rental ID: " + rentalId;
                    return ResponseEntity.ok(responseMessage);

                } catch (NotFoundException e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Lỗi hệ thống khi cập nhật trạng thái thanh toán: " + e.getMessage());
                }
            } else {
                // Giao dịch thất bại
                // TODO: Cập nhật trạng thái payment/booking nếu cần và chuyển hướng
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Giao dịch thất bại. Mã lỗi: " + vnp_ResponseCode);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chữ ký không hợp lệ!");
        }
    }

    @GetMapping("/api/get-rental-by-booking")
    @Operation(
            summary = "Get rental ID by booking ID",
            description = "Retrieves rental ID for a given booking after successful payment")
    public ResponseEntity<?> getRentalByBooking(@RequestParam Long bookingId) {
        try {
            // Query rental trực tiếp từ bookingId
            var booking = bookingService.getBookingById(bookingId);
            var rentalService_local = rentalService;

            // Lấy tất cả rentals và tìm cái match với bookingId
            var allRentals = rentalService_local.getAllRentals();
            var rentalOptional = allRentals.stream()
                    .filter(rental -> rental.getBookingId() != null && rental.getBookingId().equals(bookingId))
                    .findFirst();

            if (rentalOptional.isPresent()) {
                Long rentalId = rentalOptional.get().getRentalId();
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("rentalId", rentalId);
                response.put("bookingId", bookingId);
                response.put("message", "Lấy Rental ID thành công!");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy rental cho booking này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
