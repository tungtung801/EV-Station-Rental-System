package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j; // 1. Dùng Logger
import org.springframework.security.core.context.SecurityContextHolder; // 2. Dùng Security
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.request.PaymentStatusUpdateRequest;
import spring_boot.project_swp.dto.response.PaymentResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.BadRequestException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.PaymentMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.PaymentService;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j // Annotation để log
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
        // 1. BẢO MẬT: Lấy User đang đăng nhập từ Token (Thay vì tin tưởng Request)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User payer = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found (Auth error)"));

        Payment payment = paymentMapper.toPayment(request);
        payment.setPayer(payer); // Gán người trả tiền chuẩn xác

        // 2. Gắn Booking hoặc Rental
        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new NotFoundException("Booking not found"));
            payment.setBooking(booking);
        } else if (request.getRentalId() != null) {
            Rental rental = rentalRepository.findById(request.getRentalId())
                    .orElseThrow(() -> new NotFoundException("Rental not found"));
            payment.setRental(rental);
        } else {
            throw new BadRequestException("Must provide Booking ID or Rental ID");
        }

        payment.setStatus(PaymentStatusEnum.PENDING);
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Xác nhận thanh toán (Offline - Staff xác nhận tiền mặt)
    @Override
    @Transactional
    public PaymentResponse confirmPayment(Long paymentId, Long staffId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        payment.setStatus(PaymentStatusEnum.SUCCESS);
        payment.setConfirmedBy(staff);
        payment.setConfirmedAt(LocalDateTime.now());

        // Logic xử lý sau khi tiền về
        handlePostPaymentSuccess(payment, staffId);

        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // Update trạng thái (Online - VNPay Callback)
    @Override
    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        payment.setStatus(request.getStatus());

        if (request.getStatus() == PaymentStatusEnum.SUCCESS) {
            payment.setConfirmedAt(LocalDateTime.now());
            // Logic xử lý sau khi tiền về (không có staffId vì là hệ thống tự động)
            handlePostPaymentSuccess(payment, null);
        }
        return paymentMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    // --- HÀM HELPER: Xử lý logic tự động ---
    private void handlePostPaymentSuccess(Payment payment, Long staffId) {
        if (payment.getBooking() != null) {
            Booking booking = payment.getBooking();

            // 1. Update Booking -> CONFIRMED
            if (booking.getStatus() == BookingStatusEnum.PENDING) {
                booking.setStatus(BookingStatusEnum.CONFIRMED);
                bookingRepository.save(payment.getBooking());

                // 2. TẠO RENTAL (Phiếu thuê)
                try {
                    if (staffId != null) {
                        // Nếu có nhân viên xác nhận (Tiền mặt)
                        rentalService.createRentalFromBooking(booking.getBookingId(), staffId);
                    } else {
                        // Nếu là Online (Tự động)
                        rentalService.createRentalFromBookingAuto(booking.getBookingId());
                    }
                } catch (Exception e) {
                    // Log lỗi chứ không in ra màn hình console
                    log.error("Auto-create Rental failed: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public List<PaymentResponse> getPaymentsByBookingId(Long bookingId) {
        return paymentMapper.toPaymentResponseList(paymentRepository.findByBooking_BookingId(bookingId));
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentMapper.toPaymentResponseList(paymentRepository.findAll());
    }


    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        spring_boot.project_swp.entity.Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new spring_boot.project_swp.exception.NotFoundException("Payment not found with ID: " + paymentId));

        return paymentMapper.toPaymentResponse(payment);
    }
}
