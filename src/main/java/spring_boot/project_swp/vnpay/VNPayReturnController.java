package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "VNPay APIs")
public class VNPayReturnController {

  @Value("${vnpay.hashSecret}")
  private String hashSecret;

  final PaymentService paymentService;
  final BookingService bookingService;

  @GetMapping("/vnpay_return")
  public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
    // 1. Lấy và Xóa SecureHash ra khỏi params để check chữ ký
    String vnp_SecureHash = allParams.get("vnp_SecureHash");
    if (allParams.containsKey("vnp_SecureHashType")) {
      allParams.remove("vnp_SecureHashType");
    }
    allParams.remove("vnp_SecureHash");

    // 2. Sắp xếp tham số và tạo chuỗi hash data
    List<String> fieldNames = new ArrayList<>(allParams.keySet());
    Collections.sort(fieldNames);
    StringBuilder hashData = new StringBuilder();
    Iterator<String> itr = fieldNames.iterator();
    while (itr.hasNext()) {
      String fieldName = itr.next();
      String fieldValue = allParams.get(fieldName);
      if ((fieldValue != null) && (fieldValue.length() > 0)) {
        // Build hash data
        hashData.append(fieldName);
        hashData.append('=');
        try {
          hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        } catch (UnsupportedEncodingException e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding Error");
        }
        if (itr.hasNext()) {
          hashData.append('&');
        }
      }
    }

    // 3. Validate Chữ ký (Checksum)
    String calculatedHash = VnpayUtils.hmacSHA512(hashSecret, hashData.toString());

    if (calculatedHash.equals(vnp_SecureHash)) {
      // Checksum hợp lệ -> Kiểm tra trạng thái giao dịch
      String vnp_ResponseCode = allParams.get("vnp_ResponseCode");

      if ("00".equals(vnp_ResponseCode)) {
        // Giao dịch THÀNH CÔNG (Success)
        try {
          // Parse ID từ các trường tham chiếu
          String vnp_TxnRef = allParams.get("vnp_TxnRef"); // PAY_123_timestamp
          Long paymentId = Long.parseLong(vnp_TxnRef.split("_")[1]);

          // Lấy BookingID từ vnp_OrderInfo (Format: "Thanh toan Booking:123")
          String orderInfo = allParams.get("vnp_OrderInfo");
          Long bookingId = Long.parseLong(orderInfo.split(":")[1].trim());

          // A. Update Payment -> SUCCESS
          PaymentStatusUpdateRequest payReq = new PaymentStatusUpdateRequest();
          payReq.setStatus(PaymentStatusEnum.SUCCESS);
          paymentService.updatePaymentStatus(paymentId, payReq);

          // B. Update Booking -> CONFIRMED
          BookingStatusUpdateRequest bookReq = new BookingStatusUpdateRequest();
          bookReq.setStatus(BookingStatusEnum.CONFIRMED);

          // Dùng email admin làm đại diện hệ thống update
          bookingService.updateBookingStatus(bookingId, "admin@gmail.com", bookReq);

          return ResponseEntity.ok(
              "Thanh toán thành công! Đơn hàng #" + bookingId + " đã được xác nhận.");

        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Lỗi xử lý dữ liệu: " + e.getMessage());
        }
      } else {
        // Giao dịch THẤT BẠI (Do thẻ lỗi, hủy, v.v...)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Giao dịch thất bại. Mã lỗi VNPay: " + vnp_ResponseCode);
      }
    } else {
      // Checksum SAI -> Có dấu hiệu giả mạo
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body("Chữ ký không hợp lệ (Invalid Checksum)!");
    }
  }
}
