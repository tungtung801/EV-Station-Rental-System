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
}
