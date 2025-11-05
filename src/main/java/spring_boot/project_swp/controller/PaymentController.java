package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Payment APIs", description = "APIs for managing payments")
public class PaymentController {
  PaymentService paymentService;
  @Lazy BookingService bookingService;
  BookingMapper bookingMapper;

  @PostMapping("/deposit/{bookingId}")
  @Operation(
      summary = "Create a deposit payment",
      description = "Creates a new deposit payment for a booking.")
  public ResponseEntity<PaymentResponse> createDepositPayment(
      @PathVariable Long bookingId, @Valid @RequestBody PaymentRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    // Lấy đối tượng Booking từ bookingService và chuyển đổi sang Booking entity
    Booking booking = bookingMapper.toBooking(bookingService.getBookingById(bookingId));
    return new ResponseEntity<>(
        paymentService.createDepositPayment(booking, userEmail, request), HttpStatus.CREATED);
  }

  @PostMapping("/final/{rentalId}")
  @Operation(
      summary = "Create a final payment",
      description = "Creates a new final payment for a rental.")
  public ResponseEntity<PaymentResponse> createFinalPayment(
      @PathVariable Long rentalId, @Valid @RequestBody PaymentRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    return new ResponseEntity<>(
        paymentService.createFinalPayment(rentalId, userEmail, request), HttpStatus.CREATED);
  }

  @PatchMapping("/{paymentId}/status")
  @Operation(
      summary = "Update payment status",
      description = "Updates the status of an existing payment.")
  public ResponseEntity<PaymentResponse> updatePaymentStatus(
      @PathVariable Long paymentId, @Valid @RequestBody PaymentStatusUpdateRequest request) {
    PaymentStatusEnum paymentStatus = request.getStatus();
    PaymentResponse updatedPayment = paymentService.updatePaymentStatus(paymentId, paymentStatus);
    return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
  }

  @GetMapping("/{paymentId}")
  @Operation(
      summary = "Get payment by ID",
      description = "Retrieves a payment record by its unique ID.")
  public ResponseEntity<PaymentResponse> findPaymentById(@PathVariable Long paymentId) {
    PaymentResponse paymentResponse = paymentService.findPaymentById(paymentId);
    return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
  }

  @GetMapping("/transaction/{transactionCode}")
  @Operation(
      summary = "Get payment by transaction code",
      description = "Retrieves a payment record by its transaction code.")
  public ResponseEntity<PaymentResponse> findPaymentByTransactionCode(
      @PathVariable String transactionCode) {
    PaymentResponse paymentResponse = paymentService.findPaymentByTransactionCode(transactionCode);
    return new ResponseEntity<>(paymentResponse, HttpStatus.OK);
  }
}
