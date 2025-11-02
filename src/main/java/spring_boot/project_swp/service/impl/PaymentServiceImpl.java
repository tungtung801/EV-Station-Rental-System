package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;
  private final RentalRepository rentalRepository;
  private final UserService userService;
  private final UserMapper userMapper;

  @Override
  public PaymentResponse createPayment(PaymentRequest request) {
    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () -> new NotFoundException("Rental not found with id: " + request.getRentalId()));

    UserResponse userResponse = userService.getUserById(request.getUserId());
    if (userResponse == null) {
      throw new NotFoundException("User not found with id: " + request.getUserId());
    }
    User user = userMapper.toEntity(userResponse);

    // --- Payment calculation logic using double (as requested) ---
    // NOTE: Using double can cause inaccuracies due to rounding errors.
    // RECOMMENDATION: Use BigDecimal for currency to avoid precision issues.
    double amountToPay = 0.0;
    double totalCost = rental.getTotalCost(); // Assuming rental.getTotalCost() returns a double

    if (request.getPaymentType() == PaymentTypeEnum.DEPOSIT) {
      // Example: 10% deposit
      amountToPay = totalCost * 0.10;
    } else if (request.getPaymentType() == PaymentTypeEnum.FINAL_PAYMENT) {
      // Example: Remaining 90% payment
      amountToPay = totalCost * 0.90;
    } else {
      // Handle other types of fees
      amountToPay = totalCost;
    }

    Payment payment = new Payment();
    payment.setRental(rental);
    payment.setUser(user);
    payment.setPaymentType(request.getPaymentType());
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setAmount(amountToPay); // Entity can still be double
    payment.setStatus(PaymentStatusEnum.PENDING);
    payment.setTransactionTime(java.time.LocalDateTime.now());

    Payment savedPayment = paymentRepository.save(payment);
    log.info(
        "Created new payment with id: {} for rental: {}",
        savedPayment.getPaymentId(),
        savedPayment.getRental().getRentalId());
    return paymentMapper.toPaymentResponse(savedPayment);
  }

  @Override
  public PaymentResponse findPaymentById(Long paymentId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));
    return paymentMapper.toPaymentResponse(payment);
  }

  @Override
  public List<PaymentResponse> getPaymentsByRentalId(Long rentalId) {
    List<Payment> payments = paymentRepository.findAllByRental_RentalId(rentalId);
    List<PaymentResponse> paymentResponseList = new ArrayList<>();
    for (Payment payment : payments) {
      paymentResponseList.add(paymentMapper.toPaymentResponse(payment));
    }
    return paymentResponseList;
  }

  @Override
  public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusEnum status) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found with id: " + paymentId));
    payment.setStatus(status);
    Payment updatedPayment = paymentRepository.save(payment);
    log.info("Updated payment status to {} for payment id: {}", status, paymentId);
    return paymentMapper.toPaymentResponse(updatedPayment);
  }

  @Override
  public PaymentResponse findPaymentByTransactionCode(String transactionCode) {
    Payment payment =
        paymentRepository
            .findByTransactionCode(transactionCode)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Payment not found with transaction code: " + transactionCode));
    return paymentMapper.toPaymentResponse(payment);
  }

  @Override
  public Payment savePayment(Payment payment) {
    return paymentRepository.save(payment);
  }
}
