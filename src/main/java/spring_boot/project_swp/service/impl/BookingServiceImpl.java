package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.UserProfileRepository;
import spring_boot.project_swp.repository.VehicleRepository;
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

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new NotFoundException("User profile not found for user: " + user.getFullName()));

        if ((userProfile.getDrivingLicenseUrl() == null || userProfile.getDrivingLicenseUrl().isEmpty()) &&
            (userProfile.getIdCardUrl() == null || userProfile.getIdCardUrl().isEmpty())) {
            throw new ConflictException("User must upload either driving license or ID card before booking a vehicle.");
        }

        // Check for overlapping bookings for the same vehicle
        List<Booking> existingBookings = bookingRepository.findAll();
        for (Booking existingBooking : existingBookings) {
            if (existingBooking.getVehicle().getVehicleId() == request.getVehicleId() &&
                    !existingBooking.getStatus().equals(BookingStatusEnum.CANCELLED) &&
                    isOverlapping(request.getStartTime(), request.getEndTime(), existingBooking.getStartTime(), existingBooking.getEndTime())) {
                throw new ConflictException("Vehicle is already booked for the requested time slot");
            }
        }

        Booking booking = bookingMapper.toBooking(request);
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setStatus(BookingStatusEnum.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setTotalAmount(calculateTotalAmount(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

        return bookingMapper.toBookingResponse(bookingRepository.save(booking));
    }

    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
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
    public List<BookingResponse> getBookingsByUserId(Integer userId) {
        List<Booking> bookings = bookingRepository.findByUserUserId(userId);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (Booking booking : bookings) {
            bookingResponses.add(bookingMapper.toBookingResponse(booking));
        }
        return bookingResponses;
    }

    @Override
    public BookingResponse updateBooking(Integer bookingId, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));

        // Check for overlapping bookings for the same vehicle, excluding the current booking
        List<Booking> allBookings = bookingRepository.findAll();
        for (Booking otherBooking : allBookings) {
            if (!otherBooking.getBookingId().equals(bookingId) &&
                    otherBooking.getVehicle().getVehicleId() == request.getVehicleId() &&
                    !otherBooking.getStatus().equals(BookingStatusEnum.CANCELLED) &&
                    isOverlapping(request.getStartTime(), request.getEndTime(), otherBooking.getStartTime(), otherBooking.getEndTime())) {
                throw new ConflictException("Vehicle is already booked for the requested time slot");
            }
        }

        bookingMapper.updateBookingFromRequest(request, existingBooking);
        existingBooking.setUser(user);
        existingBooking.setVehicle(vehicle);
        existingBooking.setTotalAmount(calculateTotalAmount(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

        return bookingMapper.toBookingResponse(bookingRepository.save(existingBooking));
    }

    @Override
    public void updateBookingStatus(Integer bookingId, BookingStatusEnum status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.getStatus().equals(status)) {
            throw new ConflictException("Booking is already in " + status.name() + " status");
        }

        // Add specific business logic for status transitions if needed
        if (status.equals(BookingStatusEnum.CONFIRMED) && booking.getStatus().equals(BookingStatusEnum.CANCELLED)) {
            throw new ConflictException("Cannot confirm a cancelled booking");
        }
        if (status.equals(BookingStatusEnum.CANCELLED) && booking.getStatus().equals(BookingStatusEnum.CONFIRMED)) {
            throw new ConflictException("Cannot cancel a confirmed booking");
        }

        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingResponse> get3OnGoingBookingsOfVehicle(Integer vehicleId) {
        vehicleRepository.findById(vehicleId).orElseThrow(() -> new NotFoundException("Vehicle not found"));

        List<Booking> activeBookings = bookingRepository.findTop3ByVehicleVehicleIdAndStatusAndEndTimeGreaterThanOrderByStartTimeAsc(vehicleId, BookingStatusEnum.CONFIRMED, LocalDateTime.now());
        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : activeBookings) {
            BookingResponse response = bookingMapper.toBookingResponse(booking);
            bookingResponses.add(response);
        }
        return bookingResponses;
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private Double calculateTotalAmount(LocalDateTime startTime, LocalDateTime endTime, Double pricePerHour) {
        long durationHours = java.time.temporal.ChronoUnit.HOURS.between(startTime, endTime);
        if (durationHours < 0) {
            throw new ConflictException("End time cannot be before start time");
        }
        return durationHours * pricePerHour;
    }
}