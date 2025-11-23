package spring_boot.project_swp.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;
import spring_boot.project_swp.entity.Station;
import spring_boot.project_swp.entity.StationStatusEnum;
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
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleServiceImpl implements VehicleService {

  final VehicleRepository vehicleRepository;
  final VehicleModelRepository vehicleModelRepository;
  final StationRepository stationRepository;
  final VehicleMapper vehicleMapper;
  final FileStorageService fileService;

  @Override
  @Transactional
  public VehicleResponse addVehicle(VehicleRequest request) {
    if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
      throw new ConflictException("License plate " + request.getLicensePlate() + " already exists");
    }

    Vehicle vehicle = vehicleMapper.toVehicle(request);


    VehicleModel model =
        vehicleModelRepository
            .findById(request.getModelId())
            .orElseThrow(
                () -> new NotFoundException("Vehicle model not found: " + request.getModelId()));

    Station station =
        stationRepository
            .findById(request.getStationId())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Station not found: "
                            + request
                                .getStationId())); // Sửa findStationByStationId -> findById (Jpa
                                                   // chuẩn)

    // Check if station is active
    if (station.getIsActive() == null || !station.getIsActive().equals(StationStatusEnum.ACTIVE)) {
      throw new ConflictException(
          "Cannot add vehicle to an inactive station. "
              + "Please select an active station.");
    }

    vehicle.setVehicleModel(model);
    vehicle.setStation(station);

    if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
      String imageUrl = fileService.saveFile(request.getImageFile());
      vehicle.setImageUrl(imageUrl);
    }

    return vehicleMapper.toVehicleResponse(vehicleRepository.save(vehicle));
  }

  @Override
  @Transactional
  public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request) {
    Vehicle existingVehicle =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Vehicle not found with ID: " + id));

    // Check trùng biển số
    if (request.getLicensePlate() != null
        && !request.getLicensePlate().equals(existingVehicle.getLicensePlate())) {
      if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
        throw new ConflictException(
            "License plate '" + request.getLicensePlate() + "' already exists.");
      }
    }

    vehicleMapper.updateVehicleFromRequest(request, existingVehicle);


    if (request.getModelId() != null) {
      VehicleModel model =
          vehicleModelRepository
              .findById(request.getModelId())
              .orElseThrow(() -> new NotFoundException("Vehicle model not found"));
      existingVehicle.setVehicleModel(model);
    }

    if (request.getStationId() != null) {
      Station station =
          stationRepository
              .findById(request.getStationId())
              .orElseThrow(() -> new NotFoundException("Station not found"));

      // Check if station is active
      if (station.getIsActive() == null || !station.getIsActive().equals(StationStatusEnum.ACTIVE)) {
        throw new ConflictException(
            "Cannot assign vehicle to an inactive station. "
                + "Please select an active station.");
      }

      existingVehicle.setStation(station);
    }

    if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
      String imageUrl = fileService.saveFile(request.getImageFile());
      existingVehicle.setImageUrl(imageUrl);
    }

    return vehicleMapper.toVehicleResponse(vehicleRepository.save(existingVehicle));
  }

  @Override
  @Transactional
  public void deleteVehicle(Long vehicleId) {
    if (!vehicleRepository.existsById(vehicleId)) {
      throw new NotFoundException("Vehicle not found with ID: " + vehicleId);
    }
    vehicleRepository.deleteById(vehicleId);
  }

  @Override
  public List<VehicleResponse> findAll() {
    List<Vehicle> vehicles = vehicleRepository.findAll();
    List<VehicleResponse> responses = new ArrayList<>();
    for (Vehicle v : vehicles) {
      responses.add(vehicleMapper.toVehicleResponse(v));
    }
    return responses;
  }

  @Override
  public VehicleResponse findById(Long vehicleId) {
    Vehicle vehicle =
        vehicleRepository
            .findById(vehicleId)
            .orElseThrow(() -> new NotFoundException("Vehicle not found"));
    return vehicleMapper.toVehicleResponse(vehicle);
  }

    @Override
    public List<VehicleResponse> searchAvailableVehicles(Long stationId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        // 1. Lấy tất cả xe AVAILABLE ở trạm (Dữ liệu thô)
        List<Vehicle> allVehicles = vehicleRepository.findByStation_StationIdAndVehicleStatus(
                stationId,
                spring_boot.project_swp.entity.VehicleStatusEnum.AVAILABLE
        );

        List<Vehicle> availableVehicles = new ArrayList<>();

        // 2. Vòng lặp kiểm tra từng xe
        for (Vehicle vehicle : allVehicles) {
            boolean isBusy = false;

            // CHECK TRÊN DANH SÁCH BOOKING (KẾ HOẠCH)
            if (vehicle.getBookings() != null && !vehicle.getBookings().isEmpty()) {
                for (spring_boot.project_swp.entity.Booking booking : vehicle.getBookings()) {

                    // Chỉ check những đơn ĐANG HOẠT ĐỘNG
                    // Loại bỏ: CANCELLED (đã hủy), COMPLETED (đã xong)
                    String status = booking.getStatus().name();

                    // Các trạng thái này coi là "Đang chiếm giữ xe"
                    if (status.equals("PENDING") || status.equals("CONFIRMED") || status.equals("IN_PROGRESS")) {

                        // LOGIC CHECK TRÙNG LỊCH (OVERLAP)
                        // Dùng startTime và endTime của Booking (Đây là ngày dự kiến chuẩn nhất)

                        java.time.LocalDateTime bookedStart = booking.getStartTime();
                        java.time.LocalDateTime bookedEnd = booking.getEndTime();

                        // Công thức Overlap: (StartA <= EndB) AND (EndA >= StartB)
                        boolean isOverlap = !endDate.isBefore(bookedStart) && !startDate.isAfter(bookedEnd);

                        if (isOverlap) {
                            isBusy = true; // Xe bận
                            break; // Thoát vòng lặp ngay
                        }
                    }
                }
            }

            // 3. Nếu không bận -> Thêm vào kết quả
            if (!isBusy) {
                availableVehicles.add(vehicle);
            }
        }

        // 4. Map sang Response
        List<VehicleResponse> responses = new ArrayList<>();
        for (Vehicle v : availableVehicles) {
            responses.add(vehicleMapper.toVehicleResponse(v));
        }
        return responses;
    }

  @Override
  public List<VehicleResponse> findVehiclesByLocation(Long locationId) {
    // Get all stations for this location (including child locations)
    List<Station> stationsInLocation = stationRepository.findByLocation_LocationId(locationId);
    List<VehicleResponse> vehicleResponses = new ArrayList<>();

    for (Station station : stationsInLocation) {
      if (station.getVehicles() != null && !station.getVehicles().isEmpty()) {
        for (Vehicle vehicle : station.getVehicles()) {
          vehicleResponses.add(vehicleMapper.toVehicleResponse(vehicle));
        }
      }
    }
    return vehicleResponses;
  }

  @Override
  public List<VehicleResponse> findVehiclesByModel(String modelName) {
    // Search by model name or brand (case-insensitive)
    List<VehicleModel> models = vehicleModelRepository.findAll();
    List<Long> matchingModelIds = new ArrayList<>();

    for (VehicleModel model : models) {
      if ((model.getModelName() != null && model.getModelName().toLowerCase().contains(modelName.toLowerCase())) ||
          (model.getBrand() != null && model.getBrand().toLowerCase().contains(modelName.toLowerCase()))) {
        matchingModelIds.add(model.getModelId());
      }
    }

    List<Vehicle> vehicles = new ArrayList<>();
    for (Long modelId : matchingModelIds) {
      List<Vehicle> vehiclesForModel = vehicleRepository.findByVehicleModel_ModelId(modelId);
      if (vehiclesForModel != null) {
        vehicles.addAll(vehiclesForModel);
      }
    }

    List<VehicleResponse> responses = new ArrayList<>();
    for (Vehicle vehicle : vehicles) {
      responses.add(vehicleMapper.toVehicleResponse(vehicle));
    }
    return responses;
  }

  @Override
  public List<VehicleResponse> findVehiclesByName(String query) {
    // Search by license plate or model name (case-insensitive)
    List<Vehicle> allVehicles = vehicleRepository.findAll();
    List<VehicleResponse> matchingVehicles = new ArrayList<>();

    for (Vehicle vehicle : allVehicles) {
      boolean matches = false;

      // Check license plate
      if (vehicle.getLicensePlate() != null &&
          vehicle.getLicensePlate().toLowerCase().contains(query.toLowerCase())) {
        matches = true;
      }

      // Check model name
      if (!matches && vehicle.getVehicleModel() != null &&
          vehicle.getVehicleModel().getModelName() != null &&
          vehicle.getVehicleModel().getModelName().toLowerCase().contains(query.toLowerCase())) {
        matches = true;
      }

      if (matches) {
        matchingVehicles.add(vehicleMapper.toVehicleResponse(vehicle));
      }
    }
    return matchingVehicles;
  }
}
