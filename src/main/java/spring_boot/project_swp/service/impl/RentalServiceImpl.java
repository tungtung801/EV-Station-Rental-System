package spring_boot.project_swp.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.*;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.RentalMapper;
import spring_boot.project_swp.repository.*;
import spring_boot.project_swp.service.RentalService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RentalServiceImpl implements RentalService {

    final RentalRepository rentalRepository;
    final RentalMapper rentalMapper;
    final BookingRepository bookingRepository;
    final UserRepository userRepository;
    final VehicleRepository vehicleRepository;
    final StationRepository stationRepository;

    @Override
    public RentalResponse createRental(RentalRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        User renter = userRepository.findById(request.getRenterId())
                .orElseThrow(() -> new NotFoundException("Renter not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        Station pickupStation = stationRepository.findById(request.getPickupStationId())
                .orElseThrow(() -> new NotFoundException("Pickup Station not found"));

        Station returnStation = null;
        if (request.getReturnStationId() != null) {
            returnStation = stationRepository.findById(request.getReturnStationId())
                    .orElseThrow(() -> new NotFoundException("Return Station not found"));
        }

        User pickupStaff = null;
        if (request.getPickupStaffId() != null) {
            pickupStaff = userRepository.findById(request.getPickupStaffId())
                    .orElseThrow(() -> new NotFoundException("Pickup Staff not found"));
        }

        User returnStaff = null;
        if (request.getReturnStaffId() != null) {
            returnStaff = userRepository.findById(request.getReturnStaffId())
                    .orElseThrow(() -> new NotFoundException("Return Staff not found"));
        }

        // Check for overlapping rentals for the same vehicle
        List<Rental> existingRentals = rentalRepository.findAll();
        for (Rental existingRental : existingRentals) {
            if (existingRental.getVehicle().getVehicleId() == request.getVehicleId() &&
                    !existingRental.getStatus().equals("CANCELLED") && // Assuming CANCELLED is a status
                    isOverlapping(request.getStartTime(), request.getEndTime(), existingRental.getStartTime(), existingRental.getEndTime())) {
                throw new ConflictException("Vehicle is already rented for the requested time slot");
            }
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
        rental.setTotalCost(calculateBaseTotalCost(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

        Rental savedRental = rentalRepository.save(rental);
        return rentalMapper.toRentalResponse(recalculateTotalCost(savedRental.getRentalId()));
    }

    @Override
    public RentalResponse getRentalById(Integer rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));
        return rentalMapper.toRentalResponse(rental);
    }
    public Rental getRentalByRentalId(Integer rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));
        return rental;
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
    public List<RentalResponse> getRentalsByRenterId(Integer renterId) {
        List<Rental> rentals = rentalRepository.findByRenter_UserId(renterId);
        List<RentalResponse> rentalResponses = new ArrayList<>();
        for (Rental rental : rentals) {
            rentalResponses.add(rentalMapper.toRentalResponse(rental));
        }
        return rentalResponses;
    }

    @Override
    public List<RentalResponse> getRentalsByVehicleId(Integer vehicleId) {
        List<Rental> rentals = rentalRepository.findByVehicle_VehicleId(vehicleId);
        List<RentalResponse> rentalResponses = new ArrayList<>();
        for (Rental rental : rentals) {
            rentalResponses.add(rentalMapper.toRentalResponse(rental));
        }
        return rentalResponses;
    }

    @Override
    public RentalResponse updateRental(Integer rentalId, RentalRequest request) {
        Rental existingRental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        User renter = userRepository.findById(request.getRenterId())
                .orElseThrow(() -> new NotFoundException("Renter not found"));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new NotFoundException("Vehicle not found"));
        Station pickupStation = stationRepository.findById(request.getPickupStationId())
                .orElseThrow(() -> new NotFoundException("Pickup Station not found"));

        Station returnStation = null;
        if (request.getReturnStationId() != null) {
            returnStation = stationRepository.findById(request.getReturnStationId())
                    .orElseThrow(() -> new NotFoundException("Return Station not found"));
        }

        User pickupStaff = null;
        if (request.getPickupStaffId() != null) {
            pickupStaff = userRepository.findById(request.getPickupStaffId())
                    .orElseThrow(() -> new NotFoundException("Pickup Staff not found"));
        }

        User returnStaff = null;
        if (request.getReturnStaffId() != null) {
            returnStaff = userRepository.findById(request.getReturnStaffId())
                    .orElseThrow(() -> new NotFoundException("Return Staff not found"));
        }

        // Check for overlapping rentals for the same vehicle, excluding the current rental
        List<Rental> allRentals = rentalRepository.findAll();
        for (Rental otherRental : allRentals) {
            if (!otherRental.getRentalId().equals(rentalId) &&
                    otherRental.getVehicle().getVehicleId() == request.getVehicleId() &&
                    !otherRental.getStatus().equals("CANCELLED") &&
                    isOverlapping(request.getStartTime(), request.getEndTime(), otherRental.getStartTime(), otherRental.getEndTime())) {
                throw new ConflictException("Vehicle is already rented for the requested time slot");
            }
        }

        rentalMapper.updateRentalFromRequest(request, existingRental);
        existingRental.setBooking(booking);
        existingRental.setRenter(renter);
        existingRental.setVehicle(vehicle);
        existingRental.setPickupStation(pickupStation);
        existingRental.setReturnStation(returnStation);
        existingRental.setPickupStaff(pickupStaff);
        existingRental.setReturnStaff(returnStaff);
        existingRental.setTotalCost(calculateBaseTotalCost(request.getStartTime(), request.getEndTime(), vehicle.getPricePerHour()));

        Rental updatedRental = rentalRepository.save(existingRental);
        return rentalMapper.toRentalResponse(recalculateTotalCost(updatedRental.getRentalId()));
    }

    @Override
    public void deleteRental(Integer rentalId) {
        if (!rentalRepository.existsById(rentalId)) {
            throw new NotFoundException("Rental not found");
        }
        rentalRepository.deleteById(rentalId);
    }

    @Override
    public Rental recalculateTotalCost(Integer rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NotFoundException("Rental not found"));

        Double baseTotalCost = calculateBaseTotalCost(rental.getStartTime(), rental.getEndTime(), rental.getVehicle().getPricePerHour());
        Double totalDiscountAmount = 0.0;

        if (rental.getRentalDiscounts() != null) {
            for (RentalDiscounts rentalDiscount : rental.getRentalDiscounts()) {
                totalDiscountAmount += rentalDiscount.getAppliedAmount().doubleValue();
            }
        }

        Double finalTotalCost = baseTotalCost - totalDiscountAmount;
        if (finalTotalCost < 0) {
            finalTotalCost = 0.0; // Ensure total cost doesn't go below zero
        }
        rental.setTotalCost(finalTotalCost);
        return rentalRepository.save(rental);
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        if (end1 == null || end2 == null) { // Handle cases where end time might be null for ongoing rentals
            return false; // Or adjust logic based on how you define overlapping with open-ended rentals
        }
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private Double calculateBaseTotalCost(LocalDateTime startTime, LocalDateTime endTime, Double pricePerHour) {
        if (endTime == null) {
            return 0.0; // Or throw an exception, or calculate based on current time if rental is ongoing
        }
        long durationHours = ChronoUnit.HOURS.between(startTime, endTime);
        if (durationHours < 0) {
            throw new ConflictException("End time cannot be before start time");
        }
        return durationHours * pricePerHour;
    }
}