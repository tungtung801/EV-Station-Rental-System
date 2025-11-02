package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.VehicleChecks;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.VehicleCheckMapper;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleCheckRepository;
import spring_boot.project_swp.service.VehicleCheckService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleCheckServiceImpl implements VehicleCheckService {

  final VehicleCheckRepository vehicleCheckRepository;
  final VehicleCheckMapper vehicleCheckMapper;
  final RentalRepository rentalRepository;
  final UserRepository userRepository;

  @Override
  public VehicleCheckResponse createVehicleCheck(VehicleCheckRequest request) {
    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () -> new NotFoundException("Rental not found with ID: " + request.getRentalId()));
    User staff =
        userRepository
            .findById(request.getStaffId())
            .orElseThrow(
                () -> new NotFoundException("Staff not found with ID: " + request.getStaffId()));

    VehicleChecks vehicleChecks = vehicleCheckMapper.toVehicleChecks(request);
    vehicleChecks.setRental(rental);
    vehicleChecks.setStaff(staff);

    return vehicleCheckMapper.toVehicleCheckResponse(vehicleCheckRepository.save(vehicleChecks));
  }

  @Override
  public VehicleCheckResponse getVehicleCheckById(Long id) {
    return vehicleCheckRepository
        .findById(id)
        .map(vehicleCheckMapper::toVehicleCheckResponse)
        .orElseThrow(() -> new NotFoundException("VehicleCheck not found with ID: " + id));
  }

  @Override
  public List<VehicleCheckResponse> getAllVehicleChecks() {
    List<VehicleChecks> vehicleChecksList = vehicleCheckRepository.findAll();
    List<VehicleCheckResponse> responseList = new ArrayList<>();
    for (VehicleChecks vehicleChecks : vehicleChecksList) {
      responseList.add(vehicleCheckMapper.toVehicleCheckResponse(vehicleChecks));
    }
    return responseList;
  }

  @Override
  public VehicleCheckResponse updateVehicleCheck(Long id, VehicleCheckRequest request) {
    VehicleChecks existingVehicleCheck =
        vehicleCheckRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("VehicleCheck not found with ID: " + id));

    Rental rental =
        rentalRepository
            .findById(request.getRentalId())
            .orElseThrow(
                () -> new NotFoundException("Rental not found with ID: " + request.getRentalId()));
    User staff =
        userRepository
            .findById(request.getStaffId())
            .orElseThrow(
                () -> new NotFoundException("Staff not found with ID: " + request.getStaffId()));

    vehicleCheckMapper.updateVehicleChecks(existingVehicleCheck, request);
    existingVehicleCheck.setRental(rental);
    existingVehicleCheck.setStaff(staff);

    return vehicleCheckMapper.toVehicleCheckResponse(
        vehicleCheckRepository.save(existingVehicleCheck));
  }

  @Override
  public void deleteVehicleCheck(Long id) {
    if (!vehicleCheckRepository.existsById(id)) {
      throw new NotFoundException("VehicleCheck not found with ID: " + id);
    }
    vehicleCheckRepository.deleteById(id);
  }
}
