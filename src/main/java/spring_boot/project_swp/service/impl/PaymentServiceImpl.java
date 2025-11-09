package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.dto.response.UserResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.mapper.UserMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;
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
  @Lazy private final RentalService rentalService;
  private final BookingRepository bookingRepository;

  @Override
  public PaymentResponse createPayment(PaymentRequest request) throws ConflictException {
    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () -> new NotFoundException("Rental not found with id: " + request.getRentalId()));

    Booking booking = rental.getBooking();
    BigDecimal totalAmount = booking.getTotalAmount();
    BigDecimal depositAmount = booking.getExpectedTotal().multiply(booking.getDepositPercent());

    UserResponse userResponse = userService.getUserById(request.getUserId());
    if (userResponse == null) {
      throw new NotFoundException("User not found with id: " + request.getUserId());
    }
    User user = userMapper.toEntity(userResponse);
    user.setRole(roleService.getRoleByName(userResponse.getRoleName()));
    // User user = userMapper.toEntity(userResponse);
    // user.setRole(
    // roleService.getRoleByName(userResponse.getRoleName())); // Thêm dòng này để thiết lập Role

    // --- Payment calculation logic using double (as requested) ---
    // NOTE: Using double can cause inaccuracies due to rounding errors.
    // RECOMMENDATION: Use BigDecimal for currency to avoid precision issues.
    BigDecimal amountToPay = BigDecimal.ZERO;
    BigDecimal totalCost = rental.getTotal(); // Assuming rental.getTotal() returns a double

    // Calculate amount to pay based on payment type
    if (request.getPaymentType() == PaymentTypeEnum.DEPOSIT) {
      // Use the pre-calculated deposit amount from the booking
      amountToPay = depositAmount;
    } else if (request.getPaymentType() == PaymentTypeEnum.FINAL) {
      // For FINAL payment via CASH_ON_DELIVERY, amount is the full remaining amount
      if (request.getPaymentMethod() == PaymentMethodEnum.CASH_ON_DELIVERY) {
        amountToPay = totalCost; // Full amount for cash on delivery
      } else {
        // Calculate the remaining amount (total - deposit)
        amountToPay = totalAmount.subtract(depositAmount);
      }
    } else {
      // Handle other types of fees (e.g., penalties, additional services)
      // For now, assume it's the total amount if not deposit or final payment
      amountToPay = totalAmount;
    }

    // Handle processedByStaffId: if null, assign a default staff ID (e.g., 0L for
    // system/unassigned)
    User processedByStaff = getProcessedByStaff(request.getConfirmedById());

    Payment payment = new Payment();
    payment.setRental(rental);
    payment.setPayer(user);
    payment.setPaymentType(request.getPaymentType());
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setAmount(amountToPay);
    payment.setStatus(PaymentStatusEnum.PENDING);
    // payment.setTransactionTime(java.time.LocalDateTime.now()); // Removed as createdAt is
    // auto-generated
    payment.setConfirmedBy(processedByStaff); // Set the confirmedBy staff here

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
    List<Payment> payments = paymentRepository.findAllByRentalRentalId(rentalId);
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

    // If payment is completed, handle different payment types
    if (status.equals(PaymentStatusEnum.SUCCESS)) {
      if (payment.getPaymentType().equals(PaymentTypeEnum.DEPOSIT) && payment.getBooking() != null) {
        // For deposit payment, update booking status to DEPOSIT_PAID and create rental
        Long bookingId = payment.getBooking().getBookingId();
        Booking bookingToUpdate =
            bookingRepository
                .findById(bookingId)
                .orElseThrow(
                    () -> new NotFoundException("Booking not found with id: " + bookingId));
        bookingToUpdate.setStatus(BookingStatusEnum.DEPOSIT_PAID);
        bookingRepository.save(bookingToUpdate);

        // Create rental from booking automatically
        try {
          rentalService.createRentalFromBooking(bookingId);
          log.info("Booking {} status updated to DEPOSIT_PAID and Rental created.", bookingId);
        } catch (Exception e) {
          log.error("Error creating rental for booking {}: {}", bookingId, e.getMessage());
          // Don't throw exception here, rental creation might fail due to various reasons
          // but payment should still be marked as success
        }
      } else if (payment.getPaymentType().equals(PaymentTypeEnum.FINAL) && payment.getRental() != null) {
        // For final payment, update booking status to COMPLETED
        Long bookingId = payment.getRental().getBooking().getBookingId();
        Booking bookingToUpdate =
            bookingRepository
                .findById(bookingId)
                .orElseThrow(
                    () -> new NotFoundException("Booking not found with id: " + bookingId));
        bookingToUpdate.setStatus(BookingStatusEnum.COMPLETED);
        bookingRepository.save(bookingToUpdate);
        log.info("Booking {} status updated to COMPLETED.", bookingId);
      }
    }

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

  @Override
  public PaymentResponse createDepositPayment(
      Booking booking, String userEmail, PaymentRequest request) {
    // Tìm booking theo ID
    // Booking booking = bookingMapper.toBooking(bookingService.getBookingById(bookingId)); // Xóa
    // dòng này
    if (booking == null) {
      throw new NotFoundException("Booking not found");
    }

    // Kiểm tra trạng thái booking
    if (!booking.getStatus().equals(BookingStatusEnum.PENDING_DEPOSIT)) {
      throw new ConflictException("Booking is not in PENDING_DEPOSIT status");
    }

    // Tìm user thanh toán
    UserResponse userResponse = userService.getUserByEmail(userEmail);
    if (userResponse == null) {
      throw new NotFoundException("User not found with email: " + userEmail);
    }
    User user = userMapper.toEntity(userResponse);
    user.setRole(roleService.getRoleByName(userResponse.getRoleName()));

    // Tìm nhân viên xử lý thanh toán
    User processedByStaff = getProcessedByStaff(request.getConfirmedById());

    // Tính số tiền cọc
    BigDecimal depositAmount =
        booking
            .getExpectedTotal()
            .multiply(booking.getDepositPercent())
            .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

    // Tạo đối tượng payment
    Payment payment = new Payment();
    payment.setPayer(user);
    payment.setBooking(booking); // Lưu booking trực tiếp vào payment
    payment.setPaymentType(PaymentTypeEnum.DEPOSIT);
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setAmount(depositAmount);
    payment.setStatus(PaymentStatusEnum.PENDING);
    // payment.setTransactionTime(java.time.LocalDateTime.now()); // Removed as createdAt is
    // auto-generated
    payment.setConfirmedBy(processedByStaff);

    // Tạo mã giao dịch duy nhất cho VNPay
    String transactionCode = "BOOK" + booking.getBookingId() + "_" + System.currentTimeMillis();
    payment.setTransactionCode(transactionCode);

    // Lưu payment
    Payment savedPayment = paymentRepository.save(payment);
    log.info(
        "Created deposit payment with id: {} for booking: {}",
        savedPayment.getPaymentId(),
        booking.getBookingId());

    return paymentMapper.toPaymentResponse(savedPayment);
  }

  @Override
  public PaymentResponse createFinalPayment(
      Long rentalId, String userEmail, PaymentRequest request) {
    // Tìm rental theo ID
    Rental rental =
        rentalRepository
            .findById(rentalId)
            .orElseThrow(() -> new NotFoundException("Rental not found with id: " + rentalId));

    // Kiểm tra trạng thái rental (removed this check as final payment can happen before COMPLETED)
    // if (!rental.getStatus().equals(RentalStatusEnum.COMPLETED)) {
    //   throw new ConflictException("Rental is not in COMPLETED status");
    // }

    // Tìm user thanh toán
    UserResponse userResponse = userService.getUserByEmail(userEmail);
    if (userResponse == null) {
      throw new NotFoundException("User not found with email: " + userEmail);
    }
    User user = userMapper.toEntity(userResponse);
    user.setRole(roleService.getRoleByName(userResponse.getRoleName()));

    // Tìm nhân viên xử lý thanh toán
    User processedByStaff = getProcessedByStaff(request.getConfirmedById());

    // Lấy số tiền cuối cùng từ rental
    BigDecimal finalAmount = rental.getTotal();

    // Tạo đối tượng payment
    Payment payment = new Payment();
    payment.setRental(rental);
    payment.setPayer(user);
    payment.setPaymentType(PaymentTypeEnum.FINAL);
    payment.setPaymentMethod(request.getPaymentMethod());
    payment.setAmount(finalAmount);
    payment.setStatus(PaymentStatusEnum.PENDING);
    // payment.setTransactionTime(java.time.LocalDateTime.now()); // Removed as createdAt is
    // auto-generated
    payment.setConfirmedBy(processedByStaff);

    // Lưu payment
    Payment savedPayment = paymentRepository.save(payment);
    log.info(
        "Created final payment with id: {} for rental: {}", savedPayment.getPaymentId(), rentalId);

    return paymentMapper.toPaymentResponse(savedPayment);
  }

  // Phương thức helper lấy nhân viên xử lý thanh toán
  private User getProcessedByStaff(Long staffId) {
    User processedByStaff = null;
    if (staffId != null) {
      UserResponse staffResponse = userService.getUserById(staffId);
      if (staffResponse == null) {
        throw new NotFoundException("Staff not found with id: " + staffId);
      }
      processedByStaff = userMapper.toEntity(staffResponse);
      processedByStaff.setRole(roleService.getRoleByName(staffResponse.getRoleName()));
    } else {
      // Gán admin mặc định khi không có staffId
      UserResponse adminResponse = userService.getUserByEmail("admin@gmail.com");
      if (adminResponse == null) {
        throw new NotFoundException("Default admin user not found for processing");
      }
      processedByStaff = userMapper.toEntity(adminResponse);
      processedByStaff.setRole(roleService.getRoleByName(adminResponse.getRoleName()));
    }
    return processedByStaff;
  }

  @Override
  public Payment getPaymentByBookingId(Long bookingId){
      Payment foundPayment = paymentRepository.findById(bookingId).orElseThrow(null);
      return foundPayment;
  }
}
