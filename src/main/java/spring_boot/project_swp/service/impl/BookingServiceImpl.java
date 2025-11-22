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
    final RentalRepository rentalRepository;

    // 1. TẠO BOOKING (Giữ nguyên)
    @Override
    @Transactional
    public BookingResponse createBooking(String email, BookingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
        checkUserKYC(user.getUserId());

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found: " + request.getVehicleId()));

        validateBookingTime(request.getStartTime(), request.getEndTime());

        boolean isBusy = bookingRepository.existsByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNot(
                request.getVehicleId(), request.getEndTime(), request.getStartTime(), BookingStatusEnum.CANCELLED);

        if (isBusy) throw new ConflictException("Vehicle is busy in this time range!");

        Booking booking = bookingMapper.toBooking(request);
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setTotalAmount(calculateTotalAmount(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));
        booking.setStatus(BookingStatusEnum.PENDING);

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    // ... (Các hàm getBookingById, getAllBookings, getBookingsByUserId giữ nguyên) ...
    @Override
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public List<BookingResponse> getAllBookings(String staffEmail, Long stationId) {
        User staff = userRepository.findByEmail(staffEmail).orElseThrow(() -> new NotFoundException("Staff not found"));
        List<Booking> bookings;
        if ("Admin".equalsIgnoreCase(staff.getRole().getRoleName())) {
            bookings = bookingRepository.findAll();
        } else {
            Long targetStationId = (stationId != null) ? stationId : (staff.getStation() != null ? staff.getStation().getStationId() : null);
            if (targetStationId == null) bookings = new ArrayList<>();
            else bookings = bookingRepository.findByVehicle_Station_StationId(targetStationId);
        }
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) responses.add(bookingMapper.toBookingResponse(b));
        return responses;
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) responses.add(bookingMapper.toBookingResponse(b));
        return responses;
    }

    @Override
    public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
        List<Booking> bookings = bookingRepository.findTop3ByVehicleVehicleIdAndStatusNotAndEndTimeAfterOrderByStartTimeAsc(
                vehicleId, BookingStatusEnum.CANCELLED, LocalDateTime.now());
        List<BookingResponse> responses = new ArrayList<>();
        for (Booking b : bookings) responses.add(bookingMapper.toBookingResponse(b));
        return responses;
    }

    // 5. CẬP NHẬT TRẠNG THÁI (SỬA ĐỔI LOGIC TẠO RENTAL)
    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, String email, BookingStatusUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        User staff = userRepository.findByEmail(email).orElse(null); // Người thực hiện (Staff/Admin)
        BookingStatusEnum newStatus = request.getStatus();

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) throw new ConflictException("Cannot update a Cancelled booking");
        if (booking.getStatus() == BookingStatusEnum.COMPLETED) throw new ConflictException("Cannot update a Completed booking");

        // Cập nhật trạng thái Booking
        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        // --- [AUTO CREATE RENTAL LOGIC - NEW FLOW] ---
        // Khi trạng thái chuyển sang CONFIRMED (Đã cọc/Đã thanh toán/Staff xác nhận) -> TẠO RENTAL
        if (newStatus == BookingStatusEnum.CONFIRMED) {
            boolean rentalExists = rentalRepository.existsByBookingBookingId(bookingId);

            if (!rentalExists) {
                Rental rental = Rental.builder()
                        .booking(booking)
                        .renter(booking.getUser())
                        .vehicle(booking.getVehicle())
                        .pickupStation(booking.getVehicle().getStation())
                        .pickupStaff(staff) // Nhân viên xác nhận đơn (có thể null nếu auto)
                        // Chưa có StartActual vì chưa nhận xe
                        .status(RentalStatusEnum.PENDING_PICKUP) // Trạng thái: Chờ nhận xe
                        .total(booking.getTotalAmount()) // Lưu tạm tổng tiền dự kiến
                        .build();

                rentalRepository.save(rental);
            }
        }
        // ---------------------------------------------

        // Nếu Staff bấm "Giao xe" (IN_PROGRESS), ta cần update Rental từ PENDING_PICKUP -> ACTIVE
        if (newStatus == BookingStatusEnum.IN_PROGRESS) {
            Rental rental = rentalRepository.findByBooking_BookingId(bookingId).orElse(null);
            if (rental != null && rental.getStatus() == RentalStatusEnum.PENDING_PICKUP) {
                rental.setStatus(RentalStatusEnum.ACTIVE);
                rental.setStartActual(LocalDateTime.now());
                rental.setPickupStaff(staff); // Cập nhật nhân viên giao xe thực tế
                rentalRepository.save(rental);
            }
        }

        return bookingMapper.toBookingResponse(savedBooking);
    }

    // --- Private helpers (Giữ nguyên) ---
    private void checkUserKYC(Long userId) {
        UserProfile profile = userProfileRepository.findByUserUserId(userId).orElseThrow(() -> new NotFoundException("User Profile not found"));
        if (profile.getStatus() != UserProfileStatusEnum.VERIFIED) throw new ConflictException("User account is not verified.");
    }
    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) throw new ConflictException("End time must be after start time");
        if (Duration.between(start, end).toDays() > 30) throw new ConflictException("Cannot book more than 30 days");
    }
    private BigDecimal calculateTotalAmount(LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
        long durationMinutes = Duration.between(start, end).toMinutes();
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