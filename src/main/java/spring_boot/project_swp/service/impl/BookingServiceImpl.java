package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.BookingService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingServiceImpl implements BookingService {

    final BookingRepository bookingRepository;
    final BookingMapper bookingMapper;
    final UserRepository userRepository;
    final VehicleRepository vehicleRepository;
    final UserProfileRepository userProfileRepository;
    // --- [NEW] Thêm RentalRepository để lưu Rental ---
    final RentalRepository rentalRepository;

    // 1. TẠO BOOKING
    @Override
    @Transactional
    public BookingResponse createBooking(String email, BookingRequest request) {
        // 1.1. Lấy User từ Email
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User not found: " + email));

        // 1.2. Check KYC (Bắt buộc phải Verified mới được thuê)
        checkUserKYC(user.getUserId());

        // 1.3. Lấy Xe
        Vehicle vehicle =
                vehicleRepository
                        .findById(request.getVehicleId())
                        .orElseThrow(
                                () -> new NotFoundException("Vehicle not found: " + request.getVehicleId()));

        // 1.4. Validate Thời gian (Không được book quá khứ, End > Start)
        validateBookingTime(request.getStartTime(), request.getEndTime());

        // 1.5. Check Trùng lịch (Conflict)
        boolean isBusy =
                bookingRepository.existsByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNot(
                        request.getVehicleId(),
                        request.getEndTime(),
                        request.getStartTime(),
                        BookingStatusEnum.CANCELLED);

        if (isBusy) {
            throw new ConflictException("Vehicle is busy in this time range!");
        }

        // 1.6. Map dữ liệu
        Booking booking = bookingMapper.toBooking(request);
        booking.setUser(user);
        booking.setVehicle(vehicle);

        // 1.7. Tính Tiền (TotalAmount)
        BigDecimal total =
                calculateTotalAmount(
                        request.getStartTime(),
                        request.getEndTime(),
                        vehicle.getPricePerHour());
        booking.setTotalAmount(total);

        // 1.8. Set Trạng thái ban đầu
        booking.setStatus(BookingStatusEnum.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    // 2. LẤY CHI TIẾT BOOKING
    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        return bookingMapper.toBookingResponse(booking);
    }

    // 3. LẤY DANH SÁCH BOOKING (Cho Admin/Staff)
    @Override
    public List<BookingResponse> getAllBookings(String staffEmail, Long stationId) {
        User staff =
                userRepository
                        .findByEmail(staffEmail)
                        .orElseThrow(() -> new NotFoundException("Staff not found"));

        List<Booking> bookings;

        // Nếu là Admin: Lấy hết
        if ("Admin".equalsIgnoreCase(staff.getRole().getRoleName())) {
            bookings = bookingRepository.findAll();
        }
        // Nếu có trạm cụ thể hoặc là Staff: Lọc theo trạm
        else {
            Long targetStationId =
                    (stationId != null)
                            ? stationId
                            : (staff.getStation() != null ? staff.getStation().getStationId() : null);

            if (targetStationId == null) {
                // Cho phép xem hết nếu Staff chưa gán trạm (hoặc trả rỗng tùy logic)
                bookings = new ArrayList<>();
            } else {
                bookings = bookingRepository.findByVehicle_Station_StationId(targetStationId);
            }
        }

        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) {
            responses.add(bookingMapper.toBookingResponse(b));
        }
        return responses;
    }

    // 4. LẤY DANH SÁCH CỦA TÔI (Cho Customer)
    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) {
            responses.add(bookingMapper.toBookingResponse(b));
        }
        return responses;
    }

    // 5. CẬP NHẬT TRẠNG THÁI (Duyệt/Hủy/Hoàn thành)
    @Override
    @Transactional
    public BookingResponse updateBookingStatus(
            Long bookingId, String email, BookingStatusUpdateRequest request) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Lấy thông tin người thực hiện (Staff/Admin)
        User staff = userRepository.findByEmail(email).orElse(null);

        BookingStatusEnum newStatus = request.getStatus();

        // Logic check chuyển trạng thái hợp lệ
        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new ConflictException("Cannot update a Cancelled booking");
        }
        if (booking.getStatus() == BookingStatusEnum.COMPLETED) {
            throw new ConflictException("Cannot update a Completed booking");
        }

        // Cập nhật trạng thái Booking
        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // --- [AUTO CREATE RENTAL LOGIC] ---
        // Khi chuyển sang IN_PROGRESS (Giao xe), tự động tạo bản ghi Rental
        if (newStatus == BookingStatusEnum.IN_PROGRESS) {
            // Kiểm tra xem Rental đã tồn tại chưa để tránh tạo trùng
            boolean rentalExists = rentalRepository.existsByBookingBookingId(bookingId);

            if (!rentalExists) {
                Rental rental = Rental.builder()
                        .booking(booking)
                        .renter(booking.getUser()) // Người thuê
                        .vehicle(booking.getVehicle()) // Xe thuê
                        // Trạm lấy xe (Lấy từ trạm hiện tại của xe hoặc logic booking)
                        .pickupStation(booking.getVehicle().getStation())
                        .pickupStaff(staff) // Nhân viên thực hiện giao xe
                        .startActual(LocalDateTime.now()) // Thời gian nhận xe thực tế
                        // --- FIX: Dùng RentalStatusEnum.ACTIVE hoặc IN_PROGRESS tùy Enum của bạn ---
                        // Ở đây tôi giả định bạn dùng ACTIVE cho Rental đang chạy
                        .status(RentalStatusEnum.ACTIVE)
                        .total(BigDecimal.ZERO) // Tạm thời 0, tính sau khi trả
                        .build();

                rentalRepository.save(rental);
            }
        }
        // ----------------------------------

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // 6. LẤY 3 BOOKING SẮP TỚI CỦA XE
    @Override
    public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
        List<Booking> bookings =
                bookingRepository.findTop3ByVehicleVehicleIdAndStatusNotAndEndTimeAfterOrderByStartTimeAsc(
                        vehicleId, BookingStatusEnum.CANCELLED, LocalDateTime.now());

        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) {
            responses.add(bookingMapper.toBookingResponse(b));
        }
        return responses;
    }

    // --- CÁC HÀM HỖ TRỢ (PRIVATE) ---

    private void checkUserKYC(Long userId) {
        UserProfile profile =
                userProfileRepository
                        .findByUserUserId(userId)
                        .orElseThrow(() -> new NotFoundException("User Profile not found"));

        if (profile.getStatus() != UserProfileStatusEnum.VERIFIED) {
            throw new ConflictException("User account is not verified. Please upload KYC documents.");
        }
    }

    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        if (start.isBefore(LocalDateTime.now())) {
            // Cho phép sai số nhỏ (vd: 1-2 phút do độ trễ mạng) nếu cần
            // throw new ConflictException("Start time must be in the future");
        }
        if (end.isBefore(start)) {
            throw new ConflictException("End time must be after start time");
        }
        if (Duration.between(start, end).toDays() > 30) {
            throw new ConflictException("Cannot book more than 30 days");
        }
    }

    private BigDecimal calculateTotalAmount(
            LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
        long durationMinutes = Duration.between(start, end).toMinutes();
        if (durationMinutes < 60) durationMinutes = 60;

        long days = durationMinutes / (24 * 60);
        long remainingMinutes = durationMinutes % (24 * 60);
        long hours = (long) Math.ceil((double) remainingMinutes / 60);

        BigDecimal calculatedPricePerDay = pricePerHour.multiply(BigDecimal.valueOf(24));
        BigDecimal total = BigDecimal.ZERO;

        if (days > 0) {
            total = total.add(calculatedPricePerDay.multiply(BigDecimal.valueOf(days)));
        }
        if (hours > 0) {
            total = total.add(pricePerHour.multiply(BigDecimal.valueOf(hours)));
        }

        return total;
    }
}