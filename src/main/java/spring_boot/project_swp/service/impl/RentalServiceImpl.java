package spring_boot.project_swp.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Booking;
import spring_boot.project_swp.entity.BookingStatusEnum;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.RentalDiscounts;
import spring_boot.project_swp.entity.RentalStatusEnum;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RentalMapper;
import spring_boot.project_swp.repository.BookingRepository;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.RentalService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class RentalServiceImpl implements RentalService {

  final RentalRepository rentalRepository;
  final RentalMapper rentalMapper;
  final BookingRepository bookingRepository;
  final UserRepository userRepository;
  final VehicleRepository vehicleRepository;
  final StationRepository stationRepository;

  @Override
  public RentalResponse createRental(RentalRequest request) {
    Booking booking =
        bookingRepository
            .findById(request.getBookingId())
            .orElseThrow(() -> new NotFoundException("Booking not found"));
    User renter =
        userRepository
            .findById(request.getUserId())
            .orElseThrow(() -> new NotFoundException("Renter not found"));
    Vehicle vehicle =
        vehicleRepository
            .findById(request.getVehicleId())
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    Station pickupStation =
        stationRepository
            .findById(request.getPickupStationId())
            .orElseThrow(() -> new NotFoundException("Pickup Station not found"));

    Station returnStation = null;
    if (request.getReturnStationId() != null) {
      returnStation =
          stationRepository
              .findById(request.getReturnStationId())
              .orElseThrow(() -> new NotFoundException("Return Station not found"));
    }

    User pickupStaff = null;
    if (request.getPickupStaffId() != null) {
      pickupStaff =
          userRepository
              .findById(request.getPickupStaffId())
              .orElseThrow(() -> new NotFoundException("Pickup Staff not found"));
    }

    User returnStaff = null;
    if (request.getReturnStaffId() != null) {
      returnStaff =
          userRepository
              .findById(request.getReturnStaffId())
              .orElseThrow(() -> new NotFoundException("Return Staff not found"));
    }

    List<Rental> conflictingRentals =

      rentalRepository.findByVehicleVehicleIdAndStartActualBeforeAndEndActualAfterAndStatusNotIn(
            request.getVehicleId(),
            request.getEndActual(),
            request.getStartActual(),
            List.of(RentalStatusEnum.CANCELLED, RentalStatusEnum.COMPLETED));
    if (!conflictingRentals.isEmpty()) {
      throw new ConflictException("Vehicle is already rented for the requested time slot.");
    }

    Rental rental = rentalMapper.toRental(request);
    rental.setBooking(booking);
    rental.setRenter(renter);
    rental.setVehicle(vehicle);
    rental.setPickupStation(pickupStation);
    rental.setReturnStation(returnStation);
    rental.setPickupStaff(pickupStaff);
    rental.setReturnStaff(returnStaff);
    rental.setCreatedAt(LocalDateTime.now());

    rental.setTotal(
        calculateBaseTotalCost(
            request.getStartActual(), request.getEndActual(), vehicle.getPricePerHour()));

    Rental savedRental = rentalRepository.save(rental);
    return rentalMapper.toRentalResponse(savedRental);
  }

  @Override
  public RentalResponse createRentalFromBooking(Long bookingId) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    if (!booking.getStatus().equals(BookingStatusEnum.DEPOSIT_PAID)) {
      throw new ConflictException("Booking is not in DEPOSIT_PAID status");
    }

    // Kiểm tra xem Rental đã tồn tại cho Booking này chưa
    if (rentalRepository.findByBooking_BookingId(bookingId).isPresent()) {
      throw new ConflictException("Rental already exists for this booking");
    }

    Rental rental =
        Rental.builder()
            .booking(booking)
            .renter(booking.getUser())
            .vehicle(booking.getVehicle())
            .pickupStation(booking.getVehicle().getStation())
            .returnStation(null) // Sẽ được cập nhật khi trả xe
            .startActual(booking.getStartTime()) // Lấy từ booking
            .endActual(booking.getEndTime()) // Lấy từ booking
            .status(RentalStatusEnum.PENDING_PICKUP) // Trạng thái chờ nhận xe
            .total(booking.getExpectedTotal()) // Lấy tổng tiền từ booking
            .build();

    Rental savedRental = rentalRepository.save(rental);
    return rentalMapper.toRentalResponse(savedRental);
  }

  @Override
  public RentalResponse confirmPickup(Long bookingId, String staffEmail, String contractUrl) {
    Booking booking =
        bookingRepository
            .findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

    // Ensure booking is in DEPOSIT_PAID status before pickup
    if (!booking.getStatus().equals(BookingStatusEnum.DEPOSIT_PAID)) {
      throw new ConflictException("Booking is not in DEPOSIT_PAID status");
    }

    // Find the existing Rental associated with this booking
    Rental rental =
        rentalRepository
            .findByBooking_BookingId(bookingId)
            .orElseThrow(() -> new NotFoundException("Rental not found for this booking"));

    // Ensure rental is in PENDING_PICKUP status
    if (!rental.getStatus().equals(RentalStatusEnum.PENDING_PICKUP)) {
      throw new ConflictException("Rental is not in PENDING_PICKUP status");
    }

    User pickupStaff =
        userRepository
            .findByEmail(staffEmail)
            .orElseThrow(() -> new NotFoundException("Staff not found"));

    // Kiểm tra xem nhân viên giao xe có thuộc cùng trạm với xe không
    if (!pickupStaff
        .getStation()
        .getStationId()
        .equals(booking.getVehicle().getStation().getStationId())) {
      throw new ConflictException("Nhân viên giao xe không thuộc trạm của xe.");
    }

    // Update the existing rental details
    rental.setPickupStaff(pickupStaff);
    rental.setStartActual(LocalDateTime.now());
    rental.setStatus(RentalStatusEnum.IN_PROGRESS);
    rental.setContractUrl(contractUrl);

    // Update booking status to IN_USE after pickup
    booking.setStatus(BookingStatusEnum.IN_USE);
    bookingRepository.save(booking);

    Rental updatedRental = rentalRepository.save(rental);
    // recalculateTotal(updatedRental.getRentalId()); // Recalculate cost if needed, though it
    // should be set from booking
    return rentalMapper.toRentalResponse(updatedRental);
  }

  @Override
  public RentalResponse confirmReturn(Long rentalId, String staffEmail) {
    Rental rental =
        rentalRepository
            .findById(rentalId)
            .orElseThrow(() -> new NotFoundException("Rental not found"));

    if (!rental.getStatus().equals(RentalStatusEnum.IN_PROGRESS)) {
      throw new ConflictException("Rental is not in IN_PROGRESS status");
    }

    User returnStaff =
        userRepository
            .findByEmail(staffEmail)
            .orElseThrow(() -> new NotFoundException("Return staff not found"));

    // Thêm kiểm tra nhân viên nhận xe thuộc trạm của xe
    if (!returnStaff
        .getStation()
        .getStationId()
        .equals(rental.getVehicle().getStation().getStationId())) {
      throw new ConflictException("Nhân viên nhận xe không thuộc trạm của xe.");
    }

    rental.setEndActual(LocalDateTime.now());
    rental.setStatus(RentalStatusEnum.COMPLETED);
    rental.setReturnStaff(returnStaff);
    rental.setReturnStation(returnStaff.getStation()); // Gán returnStation theo staff

    // Cập nhật vị trí của xe sau khi trả
    Vehicle returnedVehicle = rental.getVehicle();
    returnedVehicle.setStation(returnStaff.getStation());
    vehicleRepository.save(returnedVehicle);

    Rental updatedRental = recalculateTotal(rental.getRentalId());
    return rentalMapper.toRentalResponse(updatedRental);
  }

  @Override
  public RentalResponse getRentalById(Long rentalId) {
    Rental rental =
        rentalRepository
            .findById(rentalId)
            .orElseThrow(() -> new NotFoundException("Rental not found"));
    return rentalMapper.toRentalResponse(rental);
  }

  @Override
  public List<RentalResponse> getAllRentals() {
    List<Rental> rentals = rentalRepository.findAll();
    List<RentalResponse> rentalResponses = new ArrayList<>();
    for (Rental rental : rentals) {
      rentalResponses.add(rentalMapper.toRentalResponse(rental));
    }
    return rentalResponses;
  }

  @Override
  public List<RentalResponse> getRentalsByRenterId(Long renterId) {
    List<Rental> rentals = rentalRepository.findByRenter_UserId(renterId);
    List<RentalResponse> rentalResponses = new ArrayList<>();
    for (Rental rental : rentals) {
      rentalResponses.add(rentalMapper.toRentalResponse(rental));
    }
    return rentalResponses;
  }

  @Override
  public List<RentalResponse> getRentalsByVehicleId(Long vehicleId) {
    List<Rental> rentals = rentalRepository.findByVehicle_VehicleId(vehicleId);
    List<RentalResponse> rentalResponses = new ArrayList<>();
    for (Rental rental : rentals) {
      rentalResponses.add(rentalMapper.toRentalResponse(rental));
    }
    return rentalResponses;
  }

  @Override
  public RentalResponse updateRental(Long rentalId, String userEmail, RentalRequest request) {
    Rental existingRental =
        rentalRepository
            .findById(rentalId)
            .orElseThrow(() -> new NotFoundException("Rental not found"));

    // Tìm người dùng từ email
    User renter =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException("Renter not found with email: " + userEmail));

    Booking booking =
        bookingRepository
            .findById(request.getBookingId())
            .orElseThrow(() -> new NotFoundException("Booking not found"));
    Vehicle vehicle =
        vehicleRepository
            .findById(request.getVehicleId())
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    Station pickupStation =
        stationRepository
            .findById(request.getPickupStationId())
            .orElseThrow(() -> new NotFoundException("Pickup Station not found"));

    Station returnStation = null;
    if (request.getReturnStationId() != null) {
      returnStation =
          stationRepository
              .findById(request.getReturnStationId())
              .orElseThrow(() -> new NotFoundException("Return Station not found"));
    }

    User pickupStaff = null;
    if (request.getPickupStaffId() != null) {
      pickupStaff =
          userRepository
              .findById(request.getPickupStaffId())
              .orElseThrow(() -> new NotFoundException("Pickup Staff not found"));
    }

    User returnStaff = null;
    if (request.getReturnStaffId() != null) {
      returnStaff =
          userRepository
              .findById(request.getReturnStaffId())
              .orElseThrow(() -> new NotFoundException("Return Staff not found"));
    }

    List<Rental> allRentals =
        rentalRepository.findByVehicleVehicleIdAndStartActualBeforeAndEndActualAfterAndStatusNotIn (
            request.getVehicleId(),
            request.getEndActual(),
            request.getStartActual(),
            List.of(RentalStatusEnum.CANCELLED, RentalStatusEnum.COMPLETED));
    for (Rental otherRental : allRentals) {
      if (!otherRental.getRentalId().equals(rentalId)) {
        throw new ConflictException("Vehicle is already rented for the requested time slot.");
      }
    }

    rentalMapper.updateRentalFromRequest(request, existingRental);
    existingRental.setBooking(booking);
    existingRental.setRenter(renter); // Cập nhật renter từ userEmail
    existingRental.setVehicle(vehicle);
    existingRental.setPickupStation(pickupStation);
    existingRental.setReturnStation(returnStation);
    existingRental.setPickupStaff(pickupStaff);
    existingRental.setReturnStaff(returnStaff);

    // existingRental.setTotalCost(
    //     calculateBaseTotalCost(
    //         request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

    Rental updatedRental = rentalRepository.save(existingRental);
    return rentalMapper.toRentalResponse(recalculateTotal(updatedRental.getRentalId()));
  }

  @Override
  public void deleteRental(Long rentalId) {
    if (!rentalRepository.existsById(rentalId)) {
      throw new NotFoundException("Rental not found");
    }
    rentalRepository.deleteById(rentalId);
  }

  private Rental recalculateTotal(Long rentalId) {
    Rental rental =
        rentalRepository
            .findById(rentalId)
            .orElseThrow(() -> new NotFoundException("Rental not found"));

    // Lấy giá trị booking ban đầu từ Booking.expectedTotal
    BigDecimal finalTotal = rental.getBooking().getExpectedTotal();

    // Tính phí trả muộn nếu có
    if (rental.getEndActual() != null && rental.getBooking().getEndTime() != null) {
      if (rental.getEndActual().isAfter(rental.getBooking().getEndTime())) {
        long lateHours =
            ChronoUnit.HOURS.between(rental.getBooking().getEndTime(), rental.getEndActual());
        // Giả sử phí trả muộn là 50.000 VNĐ/giờ, có thể cấu hình sau
        BigDecimal lateFeePerHour = new BigDecimal("50000.00");
        BigDecimal lateFee = lateFeePerHour.multiply(BigDecimal.valueOf(lateHours));
        finalTotal = finalTotal.add(lateFee);
      }
    }

    BigDecimal totalDiscountAmount = BigDecimal.ZERO;

    if (rental.getRentalDiscounts() != null) {
      for (RentalDiscounts rentalDiscount : rental.getRentalDiscounts()) {
        totalDiscountAmount = totalDiscountAmount.add(rentalDiscount.getAppliedAmount());
      }
    }

    finalTotal = finalTotal.subtract(totalDiscountAmount);
    if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
      finalTotal = BigDecimal.ZERO; // Ensure total cost doesn't go below zero
    }
    rental.setTotal(finalTotal);
    return rentalRepository.save(rental);
  }

  private BigDecimal calculateBaseTotalCost(
      LocalDateTime startTime, LocalDateTime endTime, BigDecimal pricePerHour) {
    if (endTime == null) {
      return BigDecimal
          .ZERO; // Or throw an exception, or calculate based on current time if rental is ongoing
    }
    long durationHours = ChronoUnit.HOURS.between(startTime, endTime);
    if (durationHours < 0) {
      throw new ConflictException("End time cannot be before start time");
    }
    return pricePerHour.multiply(BigDecimal.valueOf(durationHours));
  }
}
