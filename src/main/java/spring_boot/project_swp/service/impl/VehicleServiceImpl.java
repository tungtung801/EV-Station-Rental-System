package spring_boot.project_swp.service.impl;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.Vehicle;
import spring_boot.project_swp.entity.VehicleModel;
import spring_boot.project_swp.exception.ConflictException;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.mapper.VehicleMapper;
import spring_boot.project_swp.repository.StationRepository;
import spring_boot.project_swp.repository.VehicleModelRepository;
import spring_boot.project_swp.repository.VehicleRepository;
import spring_boot.project_swp.service.FileStorageService;
import spring_boot.project_swp.service.VehicleService;

@Service
@AllArgsConstructor
public class VehicleServiceImpl implements VehicleService {
  private final VehicleRepository vehicleRepository;
  private final VehicleModelRepository vehicleModelRepository;
  private final StationRepository stationRepository;
  private final VehicleMapper vehicleMapper;
  private final FileStorageService fileService;

  @Override
  public VehicleResponse addVehicle(VehicleRequest request) {
    if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
      throw new ConflictException(
          "Vehicle with license plate " + request.getLicensePlate() + " already exists");
    }

    Vehicle vehicle = vehicleMapper.toVehicle(request);
    VehicleModel model =
        vehicleModelRepository
            .findByModelId(request.getModelId())
            .orElseThrow(() -> new NotFoundException("Vehicle model not found"));
    Station station =
        stationRepository
            .findStationByStationId(request.getStationId())
            .orElseThrow(() -> new NotFoundException("Station not found"));
    vehicle.setVehicleModel(model);
    vehicle.setStation(station);

    if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
      String imageUrl = fileService.saveFile(request.getImageFile());
      vehicle.setImageUrl(imageUrl);
    }
    vehicle = vehicleRepository.save(vehicle);
    return vehicleMapper.toVehicleResponse(vehicle);
  }

  @Override
  public VehicleResponse updateVehicle(Long id, VehicleRequest request) {
    Optional<Vehicle> existingVehicleOptional = vehicleRepository.findById(id);
    if (existingVehicleOptional.isEmpty()) {
      throw new NotFoundException("Vehicle not found with ID: " + id);
    }

    Vehicle existingVehicle = existingVehicleOptional.get();

    Optional<Vehicle> vehicleWithSameLicensePlate =
        vehicleRepository.findByLicensePlate(request.getLicensePlate());
    if (vehicleWithSameLicensePlate.isPresent()
        && vehicleWithSameLicensePlate.get().getVehicleId() != id) {
      throw new ConflictException(
          "Vehicle with license plate '" + request.getLicensePlate() + "' already exists.");
    }

    vehicleMapper.updateVehicleFromRequest(request, existingVehicle);

    if (request.getModelId() != null) {
      VehicleModel model =
          vehicleModelRepository
              .findByModelId(request.getModelId())
              .orElseThrow(() -> new NotFoundException("Vehicle model not found"));
      existingVehicle.setVehicleModel(model);
    }

    if (request.getStationId() != null) {
      Station station =
          stationRepository
              .findStationByStationId(request.getStationId())
              .orElseThrow(() -> new NotFoundException("Station not found"));
      existingVehicle.setStation(station);
    }
    if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
      String imageUrl = fileService.saveFile(request.getImageFile());
      existingVehicle.setImageUrl(imageUrl);
    }

    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
    return vehicleMapper.toVehicleResponse(updatedVehicle);
  }

  @Override
  public void deleteVehicle(Long vehicleId) {
    if (!vehicleRepository.existsById(vehicleId)) {
      throw new NotFoundException("Vehicle not found");
    }
    vehicleRepository.deleteById(vehicleId);
  }

  @Override
  public List<VehicleResponse> findAll() {
    return vehicleMapper.toVehicleResponseList(vehicleRepository.findAll());
  }

  @Override
  public VehicleResponse findById(Long vehicleId) {
    Vehicle vehicle =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    return vehicleMapper.toVehicleResponse(vehicle);
  }
}
