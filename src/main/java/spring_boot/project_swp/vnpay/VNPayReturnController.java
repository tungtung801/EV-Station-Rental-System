package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j; // Thêm Log
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService; // THÊM MỚI
import spring_boot.project_swp.service.UserService;

@Tag(name = "VNPay APIs", description = "Các API liên quan đến xử lý thanh toán VNPay")
@RestController
@Slf4j // Thêm Log
public class VNPayReturnController {

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Autowired private PaymentService paymentService;
    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;

    @Autowired private RentalService rentalService; // THÊM MỚI

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
            log.error("VNPAY Return: Encoding error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
        }

        String mySecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());

        if (mySecureHash.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
            String vnp_TxnRef = allParams.get("vnp_TxnRef");

            if ("00".equals(vnp_ResponseCode)) {
                try {
                    // 1. Tìm payment bằng transaction code (vnp_TxnRef)
                    PaymentResponse paymentResponse = paymentService.findPaymentByTransactionCode(vnp_TxnRef);

                    // 2. Cập nhật trạng thái payment thành SUCCESS
                    paymentService.updatePaymentStatus(
                            paymentResponse.getPaymentId(), PaymentStatusEnum.SUCCESS);

                    // 3. Cập nhật trạng thái booking thành DEPOSIT_PAID
                    if (paymentResponse.getBookingId() != null) {
                        Long bookingId = paymentResponse.getBookingId();
                        String userEmail = userService.getUserById(paymentResponse.getPayerId()).getEmail();

                        BookingStatusUpdateRequest bookingStatusUpdateRequest =
                                new BookingStatusUpdateRequest();
                        bookingStatusUpdateRequest.setStatus(BookingStatusEnum.DEPOSIT_PAID);
                        bookingService.updateBookingStatus(
                                bookingId, userEmail, bookingStatusUpdateRequest);

                        // 4. *** LOGIC SỬA LỖI: TỰ ĐỘNG TẠO RENTAL ***
                        try {
                            rentalService.createRentalFromBooking(bookingId);
                            log.info("Đã tự động tạo Rental cho Booking ID: {}", bookingId);
                        } catch (Exception e) {
                            // Xử lý nếu việc tạo rental thất bại (ví dụ: đã tồn tại)
                            log.warn("Không thể tự động tạo rental cho booking {}: {}", bookingId, e.getMessage());
                        }
                        // *** KẾT THÚC LOGIC SỬA LỖI ***

                    }

                    // TODO: Chuyển hướng người dùng về trang kết quả của frontend
                    return ResponseEntity.ok("Giao dịch thành công! Cảm ơn bạn đã đặt cọc.");

                } catch (NotFoundException e) {
                    log.error("VNPAY Return: Not Found Error", e);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
                } catch (Exception e) {
                    log.error("VNPAY Return: System Error", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Lỗi hệ thống khi cập nhật trạng thái thanh toán.");
                }
            } else {
                // Giao dịch thất bại
                log.warn("VNPAY Return: Giao dịch thất bại, Mã lỗi: {}", vnp_ResponseCode);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Giao dịch thất bại. Mã lỗi: " + vnp_ResponseCode);
            }
        } else {
            log.warn("VNPAY Return: Chữ ký không hợp lệ!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Chữ ký không hợp lệ!");
        }
    }
}