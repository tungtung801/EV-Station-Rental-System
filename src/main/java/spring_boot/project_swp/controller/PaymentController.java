package spring_boot.project_swp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Tag(name = "Payment APIs", description = "APIs for managing payments")
public class PaymentController {
  final PaymentService paymentService;
  final PaymentMapper paymentMapper;

  @PostMapping("/createpayment")
  @Operation(summary = "Create a new payment", description = "Creates a new payment record.")
  public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
    return new ResponseEntity<>(paymentService.createPayment(request), HttpStatus.CREATED);
  }

  @PostMapping("/updatepayment/{paymentId}/{status}")
  @Operation(
      summary = "Update payment status",
      description = "Updates the status of an existing payment.")
  public ResponseEntity<PaymentResponse> updatePaymentStatus(
      @PathVariable Long paymentId, @PathVariable String status) {
    PaymentStatusEnum paymentStatus = PaymentStatusEnum.valueOf(status.toUpperCase());
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
