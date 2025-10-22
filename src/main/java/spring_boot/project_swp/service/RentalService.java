package spring_boot.project_swp.service;

import java.util.List;

import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;
import spring_boot.project_swp.entity.Rental;

public interface RentalService {
    RentalResponse createRental(RentalRequest request);
    RentalResponse getRentalById(Integer rentalId);
    List<RentalResponse> getAllRentals();
    List<RentalResponse> getRentalsByRenterId(Integer renterId);
    List<RentalResponse> getRentalsByVehicleId(Integer vehicleId);
    RentalResponse updateRental(Integer rentalId, RentalRequest request);
    void deleteRental(Integer rentalId);
    Rental recalculateTotalCost(Integer rentalId);
}