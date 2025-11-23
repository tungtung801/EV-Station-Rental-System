package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.BadRequestException;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class BookingServiceImpl implements BookingService {

    final BookingRepository bookingRepository;
    final BookingMapper bookingMapper;
    final UserRepository userRepository;
    final VehicleRepository vehicleRepository;
    final UserProfileRepository userProfileRepository;
    final RentalRepository rentalRepository;
    final DiscountRepository discountRepository; // Thêm Discount
    final PaymentRepository paymentRepository; // Thêm Payment
    final RentalService rentalService;

    // --- 1. TẠO BOOKING (FULL: Check Xe + Check Discount + Tạo Payment) ---
    @Override
    @Transactional
    public BookingResponse createBooking(String email, BookingRequest request) {
        // A. Validate User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        checkUserKYC(user.getUserId());

        // B. Validate Vehicle
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + request.getVehicleId()));

        if (vehicle.getVehicleStatus() != VehicleStatusEnum.AVAILABLE) {
            throw new ConflictException("Vehicle is not available. Status: " + vehicle.getVehicleStatus());
        }

        // C. Validate Time & Schedule
        validateBookingTime(request.getStartTime(), request.getEndTime());

        // Check trùng lịch: Chỉ kiểm tra những booking với trạng thái hoạt động
        // (PENDING, CONFIRMED, IN_PROGRESS) - loại trừ CANCELLED và COMPLETED
        List<BookingStatusEnum> activeStatuses = List.of(
                BookingStatusEnum.PENDING,
                BookingStatusEnum.CONFIRMED,
                BookingStatusEnum.IN_PROGRESS
        );
        boolean isBusy = bookingRepository.existsByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusIn(
                request.getVehicleId(), request.getEndTime(), request.getStartTime(), activeStatuses);
        if (isBusy) throw new ConflictException("Vehicle is busy in this time range!");

        // D. Tính tiền & Discount
        BigDecimal originalTotal = calculateTotalAmount(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour());
        BigDecimal finalTotal = originalTotal;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // Xử lý mã giảm giá
        if (request.getDiscountCode() != null && !request.getDiscountCode().trim().isEmpty()) {
            // Chỉ cho áp mã nếu thanh toán Online (Logic nghiệp vụ)
            if (request.getPaymentMethod() == PaymentMethodEnum.CASH) {
                throw new BadRequestException("Discount codes are applicable for Online Payment only");
            }

            Discount discount = discountRepository.findByCode(request.getDiscountCode())
                    .orElseThrow(() -> new NotFoundException("Discount code not found"));

            validateDiscount(discount); // Check hạn, số lượng

            // Tính tiền giảm
            if (discount.getDiscountType() == DiscountTypeEnum.PERCENTAGE) {
                BigDecimal percent = discount.getValue().divide(BigDecimal.valueOf(100));
                discountAmount = originalTotal.multiply(percent);
                if (discount.getMaxDiscountAmount() != null && discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0) {
                    discountAmount = discount.getMaxDiscountAmount();
                }
            } else {
                discountAmount = discount.getValue();
            }

            // Update số lượng mã
            discount.setCurrentUsage(discount.getCurrentUsage() + 1);
            discountRepository.save(discount);

            // Chốt giá
            finalTotal = originalTotal.subtract(discountAmount);
            if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;
        }

        // E. Lưu Booking
        Booking booking = bookingMapper.toBooking(request);
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setTotalAmount(finalTotal); // Giá sau giảm
        booking.setDiscountAmount(discountAmount);
        booking.setDiscountCode(request.getDiscountCode());
        booking.setStatus(BookingStatusEnum.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        // F. Tạo Payment PENDING (Để khách thanh toán sau)
        createInitialPayment(savedBooking, request.getPaymentMethod(), finalTotal, user);

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // --- 2. UPDATE TRẠNG THÁI (Staff/Admin duyệt đơn) ---
    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, String email, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        User staff = userRepository.findByEmail(email).orElse(null);
        BookingStatusEnum newStatus = request.getStatus();

        // Validate chuyển trạng thái
        if (booking.getStatus() == BookingStatusEnum.CANCELLED || booking.getStatus() == BookingStatusEnum.COMPLETED) {
            throw new ConflictException("Cannot update a finished booking");
        }

        // Logic Hủy đơn -> Hoàn lại mã giảm giá (nếu cần) + Giải phóng xe
        if (newStatus == BookingStatusEnum.CANCELLED) {
            // (Optional) Logic cộng lại số lượng discount nếu hủy

            // Giải phóng xe nếu đã bị chiếm dụng
            Vehicle vehicle = booking.getVehicle();
            if (vehicle != null && vehicle.getVehicleStatus() != VehicleStatusEnum.AVAILABLE) {
                vehicle.setVehicleStatus(VehicleStatusEnum.AVAILABLE);
                vehicleRepository.save(vehicle);
            }
        }

        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // AUTO CREATE RENTAL: Khi trạng thái chuyển sang CONFIRMED
        if (newStatus == BookingStatusEnum.CONFIRMED) {
            try {
                // Gọi sang RentalService để tạo phiếu thuê
                // Nếu staff != null (Staff duyệt tay) -> Gán staffId
                // Nếu staff == null (System) -> Gán null
                Long staffId = (staff != null) ? staff.getUserId() : null;

                // Nếu staffId null thì dùng hàm Auto, ngược lại dùng hàm Manual
                if (staffId == null) {
                    rentalService.createRentalFromBookingAuto(bookingId);
                } else {
                    rentalService.createRentalFromBooking(bookingId, staffId);
                }
            } catch (Exception e) {
                log.error("Auto-create Rental failed: {}", e.getMessage());
            }
        }

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // --- 3. CÁC HÀM GET ---

    @Override
    public List<BookingResponse> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        return bookingMapper.toBookingResponseList(bookingRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId()));
    }

    @Override
    public List<BookingResponse> getVehicleSchedule(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) throw new NotFoundException("Vehicle not found");
        // Lấy tất cả đơn trừ đơn Hủy
        List<Booking> list = bookingRepository.findByVehicle_VehicleIdAndStatusNot(vehicleId, BookingStatusEnum.CANCELLED);

        // Lọc bỏ COMPLETED bookings
        List<Booking> filteredList = new ArrayList<>();
        for (Booking booking : list) {
            if (booking.getStatus() != BookingStatusEnum.COMPLETED) {
                filteredList.add(booking);
            }
        }

        return bookingMapper.toBookingResponseList(filteredList);
    }

    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getAllBookings(String staffEmail, Long stationId) {
        User staff = userRepository.findByEmail(staffEmail).orElseThrow();
        List<Booking> bookings;

        if (staff.getRole().getRoleName().equalsIgnoreCase("Admin")) {
            bookings = bookingRepository.findAll();
        } else {
            // Staff chỉ xem đơn của trạm mình (hoặc trạm được filter)
            Long targetStation = (stationId != null) ? stationId : (staff.getStation() != null ? staff.getStation().getStationId() : null);
            if (targetStation == null) bookings = new ArrayList<>(); // Staff ko có trạm -> ko thấy gì
            else bookings = bookingRepository.findByVehicle_Station_StationId(targetStation);
        }
        return bookingMapper.toBookingResponseList(bookings);
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        return bookingMapper.toBookingResponseList(bookingRepository.findByUser_UserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
        // Lấy ONLY active bookings: PENDING, CONFIRMED, IN_PROGRESS
        // Exclude: COMPLETED, CANCELLED
        List<BookingStatusEnum> activeStatuses = List.of(
            BookingStatusEnum.PENDING,
            BookingStatusEnum.CONFIRMED,
            BookingStatusEnum.IN_PROGRESS
        );
        return bookingMapper.toBookingResponseList(
                bookingRepository.findTop3ByVehicleVehicleIdAndStatusInAndEndTimeAfterOrderByStartTimeAsc(
                        vehicleId, activeStatuses, LocalDateTime.now())
        );
    }

    // --- HELPER PRIVATE METHODS ---

    private void createInitialPayment(Booking booking, PaymentMethodEnum method, BigDecimal amount, User payer) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setPaymentType(PaymentTypeEnum.RENTAL_FEE);
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatusEnum.PENDING);
        payment.setPayer(payer);
        paymentRepository.save(payment);
    }

    private void checkUserKYC(Long userId) {
        UserProfile profile = userProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ConflictException("Please update your profile first"));
        if (profile.getStatus() != UserProfileStatusEnum.VERIFIED) {
            throw new ConflictException("Your account is not verified yet. Status: " + profile.getStatus());
        }
    }

    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) throw new BadRequestException("End time must be after start time");
        // Logic khác: Tối thiểu 1 tiếng, tối đa 30 ngày...
        if (Duration.between(start, end).toHours() < 1) throw new BadRequestException("Minimum rental duration is 1 hour");
    }

    private void validateDiscount(Discount discount) {
        if (!discount.getIsActive()) throw new BadRequestException("Discount code is inactive");
        if (discount.getCurrentUsage() >= discount.getUsageLimit()) throw new BadRequestException("Discount code is out of stock");
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
            throw new BadRequestException("Discount code is expired or not yet valid");
        }
    }

    private BigDecimal calculateTotalAmount(LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
        long durationMinutes = Duration.between(start, end).toMinutes();
        // Làm tròn: Dưới 1h tính 1h
        if (durationMinutes < 60) durationMinutes = 60;

        long days = durationMinutes / (24 * 60);
        long remainingMinutes = durationMinutes % (24 * 60);
        long hours = (long) Math.ceil((double) remainingMinutes / 60);

        BigDecimal calculatedPricePerDay = pricePerHour.multiply(BigDecimal.valueOf(24));
        BigDecimal total = BigDecimal.ZERO;

        if (days > 0) total = total.add(calculatedPricePerDay.multiply(BigDecimal.valueOf(days)));
        if (hours > 0) total = total.add(pricePerHour.multiply(BigDecimal.valueOf(hours)));

        return total;
    }
}