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
        User payer = userRepository.findById(request.getUserId()).orElseThrow(() -> new NotFoundException("Payer not found"));
        payment.setPayer(payer);

        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId()).orElseThrow(() -> new NotFoundException("Booking not found"));
            payment.setBooking(booking);
        }
        if (request.getRentalId() != null) {
            Rental rental = rentalRepository.findById(request.getRentalId()).orElseThrow(() -> new NotFoundException("Rental not found"));
            payment.setRental(rental);
        }
        payment.setStatus(PaymentStatusEnum.PENDING);
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Xác nhận thanh toán (Thường dùng cho Offline tại quầy)
    @Override
    @Transactional
    public PaymentResponse confirmPayment(Long paymentId, Long staffId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        User staff = userRepository.findById(staffId).orElseThrow(() -> new NotFoundException("Staff not found"));

        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setConfirmedBy(staff);
        payment.setConfirmedAt(LocalDateTime.now());

        if (payment.getBooking() != null) {
            // 1. Update Booking -> CONFIRMED
            payment.getBooking().setStatus(BookingStatusEnum.CONFIRMED);
            bookingRepository.save(payment.getBooking());

            // 2. TẠO RENTAL NGAY LẬP TỨC (PENDING_PICKUP)
            // Sử dụng createRentalFromBooking (đã có Staff)
            try {
                rentalService.createRentalFromBooking(payment.getBooking().getBookingId(), staffId);
            } catch (Exception e) {
                // Nếu Rental đã tồn tại do logic khác thì bỏ qua lỗi Conflict
                System.out.println("Rental maybe created: " + e.getMessage());
            }
        }
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Update trạng thái thanh toán (Thường dùng cho VNPay Callback)
    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setStatus(request.getStatus());

        if (request.getStatus() == PaymentStatusEnum.SUCCESS) {
            payment.setConfirmedAt(LocalDateTime.now());

            if (payment.getBooking() != null) {
                // 1. Update Booking -> CONFIRMED
                payment.getBooking().setStatus(BookingStatusEnum.CONFIRMED);
                bookingRepository.save(payment.getBooking());

                // 2. TẠO RENTAL NGAY LẬP TỨC (Auto - dùng Admin account làm staff)
                try {
                    rentalService.createRentalFromBookingAuto(payment.getBooking().getBookingId());
                } catch (Exception e) {
                    System.out.println("Rental maybe created: " + e.getMessage());
                }
            }
        }
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // ... Các hàm get giữ nguyên
    @Override
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentMapper.toPaymentResponseList(paymentRepository.findByBooking_BookingId(bookingId));
    }
    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentMapper.toPaymentResponseList(paymentRepository.findAll());
    }
}