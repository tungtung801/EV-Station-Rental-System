package spring_boot.project_swp.vnpay;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.impl.PaymentServiceImpl;

@Tag(name = "VNPay APIs", description = "Các API liên quan đến xử lý thanh toán VNPay")
@RestController
public class VNPayReturnController {

  @Value("${vnpay.hashSecret}")
  private String hashSecret;

  @Autowired private PaymentService paymentService;
  @Autowired private PaymentServiceImpl paymentServiceImpl;
  @Autowired private PaymentMapper paymentMapper;

  @Operation(
      summary = "Xử lý phản hồi trả về từ VNPay",
      description =
          "Kiểm tra chữ ký, xác nhận trạng thái thanh toán và cập nhật thông tin thanh toán trong cơ sở dữ liệu")
  @GetMapping("/vnpay_return")
  public ResponseEntity<String> handleVNPayReturn(@RequestParam Map<String, String> allParams) {
    // Get vnp_SecureHash from the response but do not remove it from the map yet
    String vnp_SecureHash = allParams.get("vnp_SecureHash");

    // Remove vnp_SecureHash from the map to prepare for re-creating the signature
    if (allParams.containsKey("vnp_SecureHash")) {
      allParams.remove("vnp_SecureHash");
    }

    // Sort parameters and re-create the hash data string
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
      // Remove the last '&'
      if (hashData.length() > 0) {
        hashData.deleteCharAt(hashData.length() - 1);
      }
    } catch (UnsupportedEncodingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Encoding error");
    }

    // Call the signature generation function from the VNPayUtils utility class
    String mySecureHash = VnpayUtils.hmacSHA512(this.hashSecret, hashData.toString());

    // ================= DEBUG LOGGING (Important for verification) =================
    System.out.println("====== VNPAY RETURN DEBUG ======");
    System.out.println("VNPAY Hash (từ URL): " + vnp_SecureHash);
    System.out.println("My Hash (tự tạo):   " + mySecureHash);
    System.out.println("Dữ liệu để hash:    " + hashData.toString());
    System.out.println("==================================");

    if (mySecureHash.equals(vnp_SecureHash)) {
      String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
      if ("00".equals(vnp_ResponseCode)) {
        System.out.println("Payment successful for order: " + allParams.get("vnp_TxnRef"));
        // TODO: Update order status in your database
        // Update payment in the database
        try {

          Long paymentId;
          try {
            paymentId = Long.valueOf(allParams.get("vnp_TxnRef"));
          } catch (NumberFormatException e) {
            System.err.println(
                "Error formatting paymentId from vnp_TxnRef: " + allParams.get("vnp_TxnRef"));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid transaction code");
          }

          PaymentResponse paymentResponse = paymentService.findPaymentById(paymentId);
          if (paymentResponse == null) {
            throw new NotFoundException("Payment not found with ID: " + paymentId);
          }

          Payment payment = paymentMapper.toPayment(paymentResponse);
          paymentService.savePayment(payment);

          // if (payment == null) {
          //     System.err.println("Payment not found with ID: " + paymentId);
          //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment information not
          // found");
          // }

          payment.setTransactionCode(allParams.get("vnp_TransactionNo"));

          // Convert vnp_PayDate from yyyyMMddHHmmss format to LocalDateTime
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
          LocalDateTime transactionTime =
              LocalDateTime.parse(allParams.get("vnp_PayDate"), formatter);
          payment.setTransactionTime(transactionTime);

          // Update payment status to successful
          paymentService.updatePaymentStatus(payment.getPaymentId(), PaymentStatusEnum.SUCCESS);

          return ResponseEntity.ok("Transaction successful!");
        } catch (Exception e) {
          System.err.println("Error updating payment: " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Error updating payment information");
        }

      } else {
        System.out.println("Payment failed. Error code: " + vnp_ResponseCode);
        // TODO: Update order status in the database
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transaction failed!");
      }
    } else {
      System.out.println(">>> ERROR: Invalid signature!");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature!");
    }
  }
}
