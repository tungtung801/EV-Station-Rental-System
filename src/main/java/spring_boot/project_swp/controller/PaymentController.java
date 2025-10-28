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
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;
import spring_boot.project_swp.service.UserService;
import spring_boot.project_swp.service.impl.PaymentServiceImpl;
import spring_boot.project_swp.service.impl.RentalServiceImpl;
import spring_boot.project_swp.service.impl.UserProfileServiceImpl;
import spring_boot.project_swp.service.impl.UserServiceImpl;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentController {
    final PaymentService paymentService;
    final RentalService rentalService;
    final RentalServiceImpl rentalServiceImpl;
    final UserService userService;
    private final UserMapper userMapper;
    private final UserProfileServiceImpl userProfileServiceImpl;
    private final UserServiceImpl userServiceIml;
    private final PaymentServiceImpl paymentServiceImpl;

    @PostMapping("/createpayment")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
//         UserResponse staff = userService.getUserById(staffId);
        try {
            User staffuser = userServiceIml.FindUserByUserId(request.getUserId());
            if (staffuser == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Rental rentalEntity = rentalServiceImpl.getRentalByRentalId(request.getRentalId());
            if (rentalEntity == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Payment payment = new Payment();
            payment.setRental(rentalEntity);
            payment.setStaffId(staffuser);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setStatus("Pending");

            return new ResponseEntity<>(paymentService.createPayment(payment), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/updatepayment/{paymentId}/{status}")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable Integer paymentId, @PathVariable String status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(paymentId, status);
        return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
    }

    @PostMapping("/{findpaymentbyId}/{paymentId}")
    public ResponseEntity<Payment> findPaymentById(@PathVariable Integer paymentId) {
        Payment payment = paymentService.findPaymentById(paymentId);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/{findpaymentbyTransactionCode}")
    public ResponseEntity<Payment> findPaymentByTransactionCode(@RequestParam String transactionCode) {
        Payment payment = paymentService.findPaymentByTransactionCode(transactionCode);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/confirmpayment/{paymentId}" )
    public ResponseEntity<Payment> confirmPayment(@PathVariable Integer paymentId) {
        Payment confirmedPayment = paymentService.confirmPayment(paymentId);
        return new ResponseEntity<>(confirmedPayment, HttpStatus.OK);
    }

    @PostMapping("/cancelpayment/{paymentId}" )
    public ResponseEntity<Payment> cancelPayment(@PathVariable Integer paymentId) {
        Payment canceledPayment = paymentService.cancelPayment(paymentId);
        return new ResponseEntity<>(canceledPayment, HttpStatus.OK);
    }

    @PostMapping("/afterPaid/{paymentId}" )
    public ResponseEntity<Payment> AfterPaid(@PathVariable Integer paymentId,@PathVariable float amount) {
        Payment afterPaidPayment = paymentService.findPaymentById(paymentId);
        afterPaidPayment.setAmount(amount);
        Payment canceledPayment = paymentServiceImpl.updatePaymentAfterPaid(afterPaidPayment);
        return new ResponseEntity<>(canceledPayment, HttpStatus.OK);
    }
}
