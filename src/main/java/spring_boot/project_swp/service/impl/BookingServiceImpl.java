package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.BookingRequest;
import spring_boot.project_swp.dto.response.BookingResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.UserProfile;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.BookingMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.BookingService;

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

  @Override
  public BookingResponse createBooking(BookingRequest request) {
    User user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
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
    booking.setStatus(BookingStatusEnum.PENDING);
    booking.setCreatedAt(LocalDateTime.now());
    booking.setTotalAmount(
        calculateTotalAmount(
            request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

    return bookingMapper.toBookingResponse(bookingRepository.save(booking));
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
  public List<BookingResponse> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
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
  public BookingResponse updateBooking(Long bookingId, BookingRequest request) {
    Booking existingBooking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    User user =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
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
    existingBooking.setTotalAmount(
        calculateTotalAmount(
            request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

    return bookingMapper.toBookingResponse(bookingRepository.save(existingBooking));
  }

  @Override
  public void updateBookingStatus(Long bookingId, BookingStatusEnum status) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    if (booking.getStatus().equals(status)) {
      throw new ConflictException("Booking is already in " + status.name() + " status");
    }

    // Add specific business logic for status transitions if needed
    if (status.equals(BookingStatusEnum.CONFIRMED)
        && booking.getStatus().equals(BookingStatusEnum.CANCELLED)) {
      throw new ConflictException("Cannot confirm a cancelled booking");
    }
    if (status.equals(BookingStatusEnum.CANCELLED)
        && booking.getStatus().equals(BookingStatusEnum.CONFIRMED)) {
      throw new ConflictException("Cannot cancel a confirmed booking");
    }

    booking.setStatus(status);
    bookingRepository.save(booking);
  }

  @Override
  public List<BookingResponse> get3OnGoingBookingsOfVehicle(Long vehicleId) {
    vehicleRepository
        .findById(vehicleId)
        .orElseThrow(() -> new NotFoundException("Vehicle not found"));

    List<Booking> activeBookings =
        bookingRepository
            .findTop3ByVehicleVehicleIdAndStatusAndEndTimeGreaterThanOrderByStartTimeAsc(
                vehicleId, BookingStatusEnum.CONFIRMED, LocalDateTime.now());
    List<BookingResponse> bookingResponses = new ArrayList<>();
    for (Booking booking : activeBookings) {
      bookingResponses.add(bookingMapper.toBookingResponse(booking));
    }
    return bookingResponses;
  }

  // --- TÍNH TOÀN BẰNG DOUBLE ---
  private Double calculateTotalAmount(
      LocalDateTime startTime, LocalDateTime endTime, Double pricePerHour) {
    long durationHours = java.time.temporal.ChronoUnit.HOURS.between(startTime, endTime);
    if (durationHours < 0) {
      throw new ConflictException("End time cannot be before start time");
    }
    return durationHours * pricePerHour;
  }
}
