package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentServiceImpl implements PaymentService {

  final PaymentRepository paymentRepository;
  final PaymentMapper paymentMapper;
  final BookingRepository bookingRepository;
  final RentalRepository rentalRepository;
  final UserRepository userRepository;
  final RentalService rentalService;

  @Override
  @Transactional
  public PaymentResponse createPayment(PaymentRequest request) {
    Payment payment = paymentMapper.toPayment(request);

    // 1. Gán người trả tiền
    User payer =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("Payer not found"));
    payment.setPayer(payer);

    // 2. Gán Booking (Nếu có)
    if (request.getBookingId() != null) {
      Booking booking =
          bookingRepository
              .findById(request.getBookingId())
              .orElseThrow(() -> new NotFoundException("Booking not found"));
      payment.setBooking(booking);
    }

    // 3. Gán Rental (Nếu có - ví dụ trả phí phát sinh)
    if (request.getRentalId() != null) {
      Rental rental =
          rentalRepository
              .findById(request.getRentalId())
              .orElseThrow(() -> new NotFoundException("Rental not found"));
      payment.setRental(rental);
    }

    // Mặc định Pending
    payment.setStatus(PaymentStatusEnum.PENDING);

    return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
  }

  @Override
  @Transactional
  public PaymentResponse confirmPayment(Long paymentId, Long staffId) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

    User staff =
        userRepository
            .findById(staffId)
            .orElseThrow(() -> new NotFoundException("Staff not found"));

    payment.setStatus(PaymentStatusEnum.SUCCESS);
    payment.setConfirmedBy(staff);
    payment.setConfirmedAt(LocalDateTime.now());

    // Update Booking status nếu thanh toán thành công
    if (payment.getBooking() != null) {
      payment.getBooking().setStatus(BookingStatusEnum.CONFIRMED);
      bookingRepository.save(payment.getBooking());

      // Tự động tạo Rental khi payment SUCCESS
      rentalService.createRentalFromBooking(payment.getBooking().getBookingId(), staffId);
    }

    return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
  }

  @Override
  @Transactional
  public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) {
    Payment payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));

    payment.setStatus(request.getStatus());

    if (request.getStatus() == PaymentStatusEnum.SUCCESS) {
      payment.setConfirmedAt(LocalDateTime.now());
      // Update Booking status nếu cần (như trên)
      if (payment.getBooking() != null) {
        payment.getBooking().setStatus(BookingStatusEnum.CONFIRMED);
        bookingRepository.save(payment.getBooking());

        // Tự động tạo Rental khi payment SUCCESS (VNPay)
        rentalService.createRentalFromBookingAuto(payment.getBooking().getBookingId());
      }
    }

    return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
  }

  @Override
  public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
    return paymentMapper.toPaymentResponseList(
        paymentRepository.findByBooking_BookingId(bookingId));
  }

  @Override
  public List<PaymentResponse> getAllPayments() {
    return paymentMapper.toPaymentResponseList(paymentRepository.findAll());
  }
}
