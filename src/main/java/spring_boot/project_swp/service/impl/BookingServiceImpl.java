package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
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
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.exception.Print_Exception.UserNotVerifiedException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.PaymentRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.BookingService;
import spring_boot.project_swp.service.PaymentService;

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
    @Lazy
    final PaymentService paymentService;
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
                        request.getVehicleId(),
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

        // Set initial status as PENDING_DEPOSIT for all booking types
        // All bookings require staff/admin confirmation before payment/rental creation
        booking.setStatus(BookingStatusEnum.PENDING_DEPOSIT);

        Booking savedBooking = bookingRepository.save(booking);

        // Create deposit payment for all booking types
        // OFFLINE: Staff will confirm later
        // ONLINE/FLEXIBLE: User pays online via VNPay
        PaymentRequest depositPaymentRequest = new PaymentRequest();
        depositPaymentRequest.setBookingId(savedBooking.getBookingId());
        depositPaymentRequest.setUserId(user.getUserId());
        depositPaymentRequest.setPaymentType(PaymentTypeEnum.DEPOSIT);

        if (request.getBookingType() == BookingTypeEnum.OFFLINE) {
            // For OFFLINE, mark as CASH payment (will be confirmed by staff)
            depositPaymentRequest.setPaymentMethod(PaymentMethodEnum.CASH_ON_DELIVERY);
        } else {
            // For ONLINE/FLEXIBLE, use bank transfer
            depositPaymentRequest.setPaymentMethod(PaymentMethodEnum.BANK_TRANSFER);
        }

        depositPaymentRequest.setAmount(
                booking.getExpectedTotal().multiply(booking.getDepositPercent())); // Số tiền cọc
        depositPaymentRequest.setNote("Deposit for booking " + savedBooking.getBookingId());

        paymentService.createDepositPayment(booking, user.getEmail(), depositPaymentRequest);

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

        List<Booking> bookings;

        // Nếu là ADMIN, hiển thị tất cả booking
        if (staff.getRole().getRoleName().equalsIgnoreCase("Admin")) {
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

        List<Booking> allBookings = conflictingBookingList(existingBooking);
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
    public BookingResponse updateBookingStatus(
            Long bookingId, BookingStatusEnum newStatus) {
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus().equals(newStatus)) {
            throw new ConflictException(
                    "Booking is already in " + newStatus + " status");
        }


        booking.setStatus(newStatus);
        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponse confirmDepositPayment(Long bookingId, String staffEmail) {

        User confirmByStaff =
                userRepository.findByEmail(staffEmail)
                        .orElseThrow(() -> new ConflictException("Staff not found with email: " + staffEmail));
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Tien hanh check phan quyen staff khi thuc hien confirm 1 booking
        // pass neu: id station staff = booking vehicle.station.id
        // fail neu nguoc lai pass.

        Station staffStation = confirmByStaff.getStation();
        Station vehicleStation = booking.getVehicle().getStation();

        if (!staffStation.equals(vehicleStation)) {
            throw new ConflictException("Staff at station '" + staffStation.getStationName() +
                    "' cannot confirm booking for a vehicle at station '" + vehicleStation.getStationName() + "'.");
        }

        // ĐÃ VƯỢT QUA 2 MÀN KIỂM TRA VÀ ĐÃ HỢP LỆ
        if (!booking.getStatus().equals(BookingStatusEnum.PENDING_DEPOSIT)) {
            throw new ConflictException("Booking is not in PENDING_DEPOSIT status.");
        }

        // Find the deposit payment for this booking
        Payment depositPayment =
                paymentRepository
                        .findByBooking_BookingIdAndPaymentType(bookingId, PaymentTypeEnum.DEPOSIT)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Deposit payment not found for booking with id: " + bookingId));

        // For all booking types (ONLINE, FLEXIBLE, OFFLINE):
        // Staff/Admin confirms the payment
        // Asign staff to depositePayment
        // Update payment status to SUCCESS
        // This will automatically:
        // 1. Update booking status to DEPOSIT_PAID
        // 2. Create rental from booking

        depositPayment.setConfirmedBy(confirmByStaff);
        paymentService.updatePaymentStatus(depositPayment.getPaymentId(), PaymentStatusEnum.SUCCESS);

        // Retrieve the updated booking after payment status update
        Booking updatedBooking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new NotFoundException("Booking not found"));

        // Explicitly update booking status to DEPOSIT_PAID
        updatedBooking.setStatus(BookingStatusEnum.DEPOSIT_PAID);
        bookingRepository.save(updatedBooking);

        // Create rental from booking after deposit payment is successful
        rentalServiceImpl.createRentalFromBooking(bookingId);

        // Retrieve the created rental
        RentalResponse rentalResponse = rentalServiceImpl.getAllRentals().stream()
                .filter(rental -> rental.getBookingId() != null && rental.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Rental not found for this booking"));

        // Create booking response and attach rental
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(updatedBooking);
        bookingResponse.setRental(rentalResponse);

        return bookingResponse;
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

    // Kiểm tra conflict giữa các booking
    private List<Booking> conflictingBookingList(Booking needCheckingBooking) {
        List<Booking> conflictingBookings =
                bookingRepository.findByVehicle_VehicleIdAndStartTimeBeforeAndEndTimeAfterAndStatusNotIn(
                        needCheckingBooking.getVehicle().getVehicleId(),
                        needCheckingBooking.getEndTime(),
                        needCheckingBooking.getStartTime(),
                        List.of(BookingStatusEnum.CANCELLED, BookingStatusEnum.COMPLETED));
        return conflictingBookings;
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
