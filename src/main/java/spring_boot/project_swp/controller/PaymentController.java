package spring_boot.project_swp.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.impl.RentalServiceImpl;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentController {
    final PaymentService paymentService;
    final RentalService rentalService;
    final RentalServiceImpl rentalServiceImpl;

    @PostMapping("/{createpayment}")
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody @Valid PaymentRequest request, Rental rental, User user) {
        Payment payment = new Payment();

        Rental rentalEntity = rentalServiceImpl.getRentalByRentalId(rental.getRentalId());
        payment.setRental(rental);
        payment.setStaffId(user);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("Pending");
        return new ResponseEntity<>(paymentService.createPayment(payment), HttpStatus.OK);
    }

    @PostMapping("/{updatepayment}")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Integer paymentId, @RequestParam String status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, status);
        return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
    }

    @PostMapping("/{findpaymentbyId}")
    public ResponseEntity<Payment> findPaymentById(@PathVariable Integer paymentId) {
        Payment payment = paymentService.findPaymentById(paymentId);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/{findpaymentbyTransactionCode}")
    public ResponseEntity<Payment> findPaymentByTransactionCode(@RequestParam String transactionCode) {
        Payment payment = paymentService.findPaymentByTransactionCode(transactionCode);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }
}
