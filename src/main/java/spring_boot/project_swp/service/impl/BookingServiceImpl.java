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
    // Tìm xem có booking nào khác đang CHƯA HỦY (Status != CANCELLED) nằm chắn giữa khoảng thời
    // gian này không
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
    if (request.getBookingType() == BookingTypeEnum.OFFLINE) {
      // Offline: Chờ khách đến nhận xe rồi thanh toán
      booking.setStatus(BookingStatusEnum.PENDING);
    } else {
      // Online: Chờ khách thanh toán (chưa confirm booking)
      booking.setStatus(BookingStatusEnum.PENDING);
    }

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
        throw new ConflictException("Staff has no station and no stationId provided");
      }
      bookings = bookingRepository.findByVehicle_Station_StationId(targetStationId);
    }

    // Convert List Entity -> List Response thủ công (hoặc dùng Mapper List)
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

    BookingStatusEnum newStatus = request.getStatus();

    // Logic check chuyển trạng thái hợp lệ
    if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
      throw new ConflictException("Cannot update a Cancelled booking");
    }
    if (booking.getStatus() == BookingStatusEnum.COMPLETED) {
      throw new ConflictException("Cannot update a Completed booking");
    }

    booking.setStatus(newStatus);
    return bookingMapper.toBookingResponse(bookingRepository.save(booking));
  }

  // 6. LẤY 3 BOOKING SẮP TỚI CỦA XE (Để hiển thị lịch bận trên UI)
  @Override
  public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
    // Logic: Lấy các booking CONFIRMED hoặc PENDING trong tương lai
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

    // Chỉ cho phép VERIFIED thuê xe
    if (profile.getStatus() != UserProfileStatusEnum.VERIFIED) {
      throw new ConflictException("User account is not verified. Please upload KYC documents.");
    }
  }

  private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
    if (start.isBefore(LocalDateTime.now())) {
      throw new ConflictException("Start time must be in the future");
    }
    if (end.isBefore(start)) {
      throw new ConflictException("End time must be after start time");
    }
    // Giới hạn thuê tối đa 30 ngày
    if (Duration.between(start, end).toDays() > 30) {
      throw new ConflictException("Cannot book more than 30 days");
    }
  }

  private BigDecimal calculateTotalAmount(
      LocalDateTime start, LocalDateTime end, BigDecimal pricePerHour) {
    long durationMinutes = Duration.between(start, end).toMinutes();
    if (durationMinutes < 60) durationMinutes = 60; // Tối thiểu 1 tiếng

    long days = durationMinutes / (24 * 60);
    long remainingMinutes = durationMinutes % (24 * 60);
    long hours = (long) Math.ceil((double) remainingMinutes / 60); // Làm tròn lên (1h10p -> 2h)

    // Tính pricePerDay từ pricePerHour (1 ngày = 24 giờ)
    BigDecimal calculatedPricePerDay = pricePerHour.multiply(BigDecimal.valueOf(24));

    BigDecimal total = BigDecimal.ZERO;

    // Cộng tiền ngày
    if (days > 0) {
      total = total.add(calculatedPricePerDay.multiply(BigDecimal.valueOf(days)));
    }
    // Cộng tiền giờ lẻ
    if (hours > 0) {
      total = total.add(pricePerHour.multiply(BigDecimal.valueOf(hours)));
    }

    return total;
  }
}
