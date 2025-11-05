package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.request.BookingStatusUpdateRequest;
import spring_boot.project_swp.dto.request.PaymentRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.dto.response.UserVerificationStatusResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.BookingTypeEnum;
import spring_boot.project_swp.entity.Payment;
import spring_boot.project_swp.entity.PaymentMethodEnum;
import spring_boot.project_swp.entity.PaymentStatusEnum;
import spring_boot.project_swp.entity.PaymentTypeEnum;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.UserProfileStatusEnum;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.exception.Print_Exception.UserNotVerifiedException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;
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
  final StationRepository stationRepository;
  @Lazy final PaymentService paymentService;
  final PaymentRepository paymentRepository;
    private final RentalServiceImpl rentalServiceImpl;

    @Override
  public BookingResponse createBooking(String email, BookingRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    // Kiểm tra xác minh người dùng trước khi tạo booking
    checkUserVerification(user.getUserId());

    Vehicle vehicle =
        vehicleRepository
            .findById(request.getVehicleId())
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));

    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(user.getUserId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "User profile not found for user: " + user.getFullName()));

    if ((userProfile.getDrivingLicenseUrl() == null || userProfile.getDrivingLicenseUrl().isEmpty())
        && (userProfile.getIdCardUrl() == null || userProfile.getIdCardUrl().isEmpty())) {
      throw new ConflictException(
          "User must upload either driving license or ID card before booking a vehicle.");
    }

    List<Booking> conflictingBookings =
        bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
            request.getVehicleId().longValue(),
            request.getEndTime(),
            request.getStartTime(),
            List.of(BookingStatusEnum.CANCELLED, BookingStatusEnum.COMPLETED));
    if (!conflictingBookings.isEmpty()) {
      throw new ConflictException("Vehicle is already booked for the requested time slot.");
    }

    Booking booking = bookingMapper.toBooking(request);
    booking.setUser(user);
    booking.setVehicle(vehicle);
    booking.setCreatedAt(LocalDateTime.now());

    BigDecimal expectedTotal =
        calculateTotalAmount(
            request.getStartTime(),
            request.getEndTime(),
            vehicle.getPricePerHour(),
            vehicle.getPricePerDay());
    booking.setExpectedTotal(expectedTotal);
    booking.setDepositPercent(BigDecimal.valueOf(0.1)); // 10% deposit
    booking.setBookingType(request.getBookingType());

    // Set initial status based on booking type
    if (request.getBookingType() == BookingTypeEnum.ONLINE
        || request.getBookingType() == BookingTypeEnum.FLEXIBLE) {
      booking.setStatus(BookingStatusEnum.PENDING_DEPOSIT);
    } else if (request.getBookingType() == BookingTypeEnum.OFFLINE) {
      booking.setStatus(BookingStatusEnum.DEPOSIT_PAID);
    }

    Booking savedBooking = bookingRepository.save(booking);

    // Create deposit payment only for ONLINE or FLEXIBLE bookings
    if (request.getBookingType() == BookingTypeEnum.ONLINE
        || request.getBookingType() == BookingTypeEnum.FLEXIBLE) {
      PaymentRequest depositPaymentRequest = new PaymentRequest();
      depositPaymentRequest.setBookingId(savedBooking.getBookingId());
      depositPaymentRequest.setUserId(user.getUserId());
      depositPaymentRequest.setPaymentType(PaymentTypeEnum.DEPOSIT);
      depositPaymentRequest.setPaymentMethod(
          PaymentMethodEnum.BANK_TRANSFER); // Mặc định là chuyển khoản ngân hàng
      depositPaymentRequest.setAmount(
          booking.getExpectedTotal().multiply(booking.getDepositPercent())); // Số tiền cọc
      depositPaymentRequest.setNote("Deposit for booking " + savedBooking.getBookingId());

     paymentService.createDepositPayment(booking, user.getEmail(), depositPaymentRequest);
    }

    return bookingMapper.toBookingResponse(savedBooking);
  }

  @Override
  public BookingResponse getBookingById(Long bookingId) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));
    return bookingMapper.toBookingResponse(booking);
  }

  @Override
  public List<BookingResponse> getAllBookings(String staffEmail, Long stationId) {
    User staff =
        userRepository
            .findByEmail(staffEmail)
            .orElseThrow(() -> new NotFoundException("Staff not found"));

    List<Booking> bookings = new ArrayList<>();

    // Nếu là ADMIN, hiển thị tất cả booking
    if (staff.getRole().getRoleName().equals("ADMIN")) {
      bookings = bookingRepository.findAll();
    } else if (stationId != null) { // Nếu có stationId được cung cấp, lọc theo stationId đó
      bookings = bookingRepository.findByVehicle_Station_StationId(stationId);
    } else if (staff.getStation()
        != null) { // Nếu không phải ADMIN và không có stationId, lọc theo station của staff
      bookings =
          bookingRepository.findByVehicle_Station_StationId(staff.getStation().getStationId());
    } else {
      throw new ConflictException(
          "Staff is not assigned to a station and no stationId was provided.");
    }

    List<BookingResponse> bookingResponses = new ArrayList<>();
    for (Booking booking : bookings) {
      bookingResponses.add(bookingMapper.toBookingResponse(booking));
    }
    return bookingResponses;
  }

  @Override
  public List<BookingResponse> getBookingsByUserId(Long userId) {
    List<Booking> bookings = bookingRepository.findByUserUserId(userId);
    List<BookingResponse> bookingResponses = new ArrayList<>();
    for (Booking booking : bookings) {
      bookingResponses.add(bookingMapper.toBookingResponse(booking));
    }
    return bookingResponses;
  }

  @Override
  public BookingResponse updateBooking(Long bookingId, String email, BookingRequest request) {
    Booking existingBooking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    Vehicle vehicle =
        vehicleRepository
            .findById(request.getVehicleId())
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));

    List<Booking> allBookings =
        bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
            request.getVehicleId().longValue(),
            request.getEndTime(),
            request.getStartTime(),
            List.of(BookingStatusEnum.CANCELLED, BookingStatusEnum.COMPLETED));
    for (Booking otherBooking : allBookings) {
      if (!otherBooking.getBookingId().equals(bookingId)) {
        throw new ConflictException("Vehicle is already booked for the requested time slot.");
      }
    }

    bookingMapper.updateBookingFromRequest(request, existingBooking);
    existingBooking.setUser(user);
    existingBooking.setVehicle(vehicle);

    BigDecimal expectedTotal =
        calculateTotalAmount(
            request.getStartTime(),
            request.getEndTime(),
            vehicle.getPricePerHour(),
            vehicle.getPricePerDay());
    existingBooking.setExpectedTotal(expectedTotal);
    existingBooking.setDepositPercent(BigDecimal.valueOf(0.1)); // Keep deposit percent consistent

    return bookingMapper.toBookingResponse(bookingRepository.save(existingBooking));
  }

  @Override
  public BookingResponse updateBookingStatus(
      Long bookingId, String email, BookingStatusUpdateRequest request) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    if (booking.getStatus().equals(request.getStatus())) {
      throw new ConflictException(
          "Booking is already in " + request.getStatus().name() + " status");
    }

    // Add specific business logic for status transitions if needed
    // if (status.equals(BookingStatusEnum.CONFIRMED)
    //     && booking.getStatus().equals(BookingStatusEnum.CANCELLED)) {
    //   throw new ConflictException("Cannot confirm a cancelled booking");
    // }
    // if (status.equals(BookingStatusEnum.CANCELLED)
    //     && booking.getStatus().equals(BookingStatusEnum.CONFIRMED)) {
    //   throw new ConflictException("Cannot cancel a confirmed booking");
    // }

    booking.setStatus(request.getStatus());
    return bookingMapper.toBookingResponse(bookingRepository.save(booking));
  }

  @Override
  public BookingResponse confirmDepositPayment(Long bookingId, String staffEmail) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    if (!booking.getStatus().equals(BookingStatusEnum.PENDING_DEPOSIT)) {
      throw new ConflictException("Booking is not in PENDING_DEPOSIT status.");
    }

    // Xử lý khác biệt giữa đặt xe trực tuyến và ngoại tuyến
    if (booking.getBookingType() == BookingTypeEnum.ONLINE) {
      // Tìm payment cọc liên quan đến booking này
      Payment depositPayment =
          paymentRepository
              .findByBooking_BookingIdAndPaymentType(bookingId, PaymentTypeEnum.DEPOSIT)
              .orElseThrow(
                  () ->
                      new NotFoundException(
                          "Deposit payment not found for booking with id: " + bookingId));

      // Cập nhật trạng thái của payment cọc thành SUCCESS
      paymentService.updatePaymentStatus(depositPayment.getPaymentId(), PaymentStatusEnum.SUCCESS);
    } else if (booking.getBookingType() == BookingTypeEnum.OFFLINE) {
      // Đặt xe ngoại tuyến không cần thanh toán đặt cọc, bỏ qua kiểm tra
      log.info("Confirming offline booking without deposit payment: {}", bookingId);
    }

    // Cập nhật trạng thái booking
    booking.setStatus(BookingStatusEnum.DEPOSIT_PAID);
      RentalResponse rentalResponse = new RentalResponse();
      rentalResponse = rentalServiceImpl.createRentalFromBooking(bookingId);
    return bookingMapper.toBookingResponse(bookingRepository.save(booking));
  }

  @Override
  public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
    vehicleRepository
        .findById(vehicleId)
        .orElseThrow(() -> new NotFoundException("Vehicle not found"));

    List<Booking> activeBookings =
        bookingRepository
            .findTop3ByVehicleVehicleIdAndStatusAndEndTimeGreaterThanOrderByStartTimeAsc(
                vehicleId, BookingStatusEnum.IN_USE, LocalDateTime.now());
    List<BookingResponse> bookingResponses = new ArrayList<>();
    for (Booking booking : activeBookings) {
      bookingResponses.add(bookingMapper.toBookingResponse(booking));
    }
    return bookingResponses;
  }

  // --- TÍNH TOÀN BẰNG DOUBLE ---
  private BigDecimal calculateTotalAmount(
      LocalDateTime startTime,
      LocalDateTime endTime,
      BigDecimal pricePerHour,
      BigDecimal pricePerDay) {
    if (endTime.isBefore(startTime)) {
      throw new ConflictException("End time cannot be before start time");
    }

    long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
    if (durationMinutes < 60) { // Minimum 1 hour rental
      durationMinutes = 60;
    }

    BigDecimal totalAmount = BigDecimal.ZERO;
    long fullDays = durationMinutes / (24 * 60);
    long remainingMinutes = durationMinutes % (24 * 60);

    totalAmount = totalAmount.add(pricePerDay.multiply(BigDecimal.valueOf(fullDays)));

    long remainingHours = (long) Math.ceil((double) remainingMinutes / 60);
    totalAmount = totalAmount.add(pricePerHour.multiply(BigDecimal.valueOf(remainingHours)));

    return totalAmount;
  }

  @Override
  public UserVerificationStatusResponse checkUserVerification(Long userId) {
    UserProfile userProfile =
        userProfileRepository
            .findByUserUserId(userId)
            .orElseThrow(() -> new NotFoundException("User profile not found"));

    if (!userProfile.getStatus().equals(UserProfileStatusEnum.VERIFIED)) {
      throw new UserNotVerifiedException("User is not verified");
    }
    return UserVerificationStatusResponse.builder()
        .isVerified(true)
        .message("User is verified")
        .build();
  }
}
