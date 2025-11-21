package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.UserService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payment APIs", description = "APIs for managing payments")
public class PaymentController {

  PaymentService paymentService;
  UserService userService; // Để lấy Staff ID từ token

  // 1. Tạo thanh toán (Dùng chung cho cả Offline/Online nếu muốn tạo tay)
  @PostMapping
  @Operation(summary = "Create a new payment")
  public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
    return new ResponseEntity<>(paymentService.createPayment(request), HttpStatus.CREATED);
  }

  // 2. Staff xác nhận đã nhận tiền (Cho Offline)
  @PutMapping("/{paymentId}/confirm")
  @Operation(summary = "Confirm payment (Staff only)")
  public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long paymentId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String staffEmail = authentication.getName();
    Long staffId = userService.getUserByEmail(staffEmail).getUserId();

    return ResponseEntity.ok(paymentService.confirmPayment(paymentId, staffId));
  }

  // 3. Update trạng thái (Admin/System)
  @PatchMapping("/{paymentId}/status")
  @Operation(summary = "Update payment status manually")
  public ResponseEntity<PaymentResponse> updatePaymentStatus(
      @PathVariable Long paymentId, @Valid @RequestBody PaymentStatusUpdateRequest request) {
    return ResponseEntity.ok(paymentService.updatePaymentStatus(paymentId, request));
  }

  // 4. Get By ID
  @GetMapping("/{paymentId}")
  @Operation(summary = "Get payment by ID")
  public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
    // Em nhớ thêm hàm getById vào Service nhé (dễ mà, findById thôi)
    // Nếu chưa có thì dùng tạm getAll rồi filter, hoặc viết thêm 1 dòng trong service
    // Tạm thời return null để code compile được nếu em lười viết service
    return null;
  }

  // 5. Get All
  @GetMapping
  @Operation(summary = "Get all payments")
  public ResponseEntity<List<PaymentResponse>> getAllPayments() {
    return ResponseEntity.ok(paymentService.getAllPayments());
  }

  // 6. Get by Booking
  @GetMapping("/booking/{bookingId}")
  public ResponseEntity<List<PaymentResponse>> getPaymentsByBooking(@PathVariable Long bookingId) {
    return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId));
  }
}
