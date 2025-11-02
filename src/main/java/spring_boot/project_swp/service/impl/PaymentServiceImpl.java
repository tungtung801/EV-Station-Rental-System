package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RoleService;
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
  private final RoleService roleService;

  @Override
  public PaymentResponse createPayment(PaymentRequest request) throws ConflictException {
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
    user.setRole(
        roleService.getRoleByName(userResponse.getRoleName())); // Thêm dòng này để thiết lập Role

    // --- Payment calculation logic using double (as requested) ---
    // NOTE: Using double can cause inaccuracies due to rounding errors.
    // RECOMMENDATION: Use BigDecimal for currency to avoid precision issues.
    double amountToPay = 0.0;
    double totalCost = rental.getTotalCost(); // Assuming rental.getTotalCost() returns a double

    // Business rule: CASH_ON_DELIVERY cannot be used with DEPOSIT
    if (request.getPaymentMethod() == PaymentMethodEnum.CASH_ON_DELIVERY
        && request.getPaymentType() == PaymentTypeEnum.DEPOSIT) {
      throw new ConflictException("CASH_ON_DELIVERY cannot be used with DEPOSIT payment type.");
    }

    // New Business rule: If CASH_ON_DELIVERY, paymentType must be FINAL_PAYMENT and amount must be
    // 100%
    if (request.getPaymentMethod() == PaymentMethodEnum.CASH_ON_DELIVERY) {
      if (request.getPaymentType() != PaymentTypeEnum.FINAL_PAYMENT) {
        throw new ConflictException("For CASH_ON_DELIVERY, payment type must be FINAL_PAYMENT.");
      }
      amountToPay = totalCost; // Full amount for cash on delivery
    } else if (request.getPaymentType() == PaymentTypeEnum.DEPOSIT) {
      // Example: 10% deposit
      amountToPay = totalCost * 0.10;
    } else if (request.getPaymentType() == PaymentTypeEnum.FINAL_PAYMENT) {
      // Example: Remaining 90% payment (for non-cash payments after deposit)
      amountToPay = totalCost * 0.90;
    } else {
      // Handle other types of fees
      amountToPay = totalCost;
    }

    // Handle processedByStaffId: if null, assign a default staff ID (e.g., 0L for
    // system/unassigned)
    User processedByStaff = null;
    if (request.getStaffId() != null) {
      UserResponse staffResponse = userService.getUserById(request.getStaffId());
      if (staffResponse == null) {
        throw new NotFoundException("Staff not found with id: " + request.getStaffId());
      }
      processedByStaff = userMapper.toEntity(staffResponse);
    } else {
      // Assign default admin user when staffId is null (ensured by DataInitializer)
      UserResponse adminResponse = userService.getUserByEmail("admin@gmail.com");
      if (adminResponse == null) {
        throw new NotFoundException("Default admin user not found for processing");
      }
      processedByStaff = userMapper.toEntity(adminResponse);
      processedByStaff.setRole(roleService.getRoleByName(adminResponse.getRoleName()));
    }

    Payment payment = new Payment();
    payment.setRental(rental);
    payment.setUser(user);
    payment.setPaymentType(request.getPaymentType());
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setAmount(amountToPay); // Entity can still be double
    payment.setStatus(PaymentStatusEnum.PENDING);
    payment.setTransactionTime(java.time.LocalDateTime.now());
    payment.setProcessedByStaff(processedByStaff); // Set the processedByStaff here

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
