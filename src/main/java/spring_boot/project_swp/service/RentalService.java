package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.RentalRequest;
import spring_boot.project_swp.dto.response.RentalResponse;

public interface RentalService {
  RentalResponse createRental(RentalRequest request);

  RentalResponse getRentalById(Long rentalId);

  List<RentalResponse> getAllRentals();

  List<RentalResponse> getRentalsByRenterId(Long renterId);

  List<RentalResponse> getRentalsByVehicleId(Long vehicleId);

  RentalResponse updateRental(Long rentalId, RentalRequest request);

  void deleteRental(Long rentalId);
}
