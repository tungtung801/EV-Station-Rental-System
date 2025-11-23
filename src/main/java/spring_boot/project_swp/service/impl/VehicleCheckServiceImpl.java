package spring_boot.project_swp.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;
import spring_boot.project_swp.entity.Rental;
import spring_boot.project_swp.entity.RentalStatusEnum;
import spring_boot.project_swp.entity.User;
import spring_boot.project_swp.entity.VehicleChecks;
import spring_boot.project_swp.exception.NotFoundException;
import spring_boot.project_swp.exception.BadRequestException;
import spring_boot.project_swp.mapper.VehicleCheckMapper;
import spring_boot.project_swp.repository.RentalRepository;
import spring_boot.project_swp.repository.UserRepository;
import spring_boot.project_swp.repository.VehicleCheckRepository;
import spring_boot.project_swp.service.VehicleCheckService;
import spring_boot.project_swp.service.FileStorageService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class VehicleCheckServiceImpl implements VehicleCheckService {

  final VehicleCheckRepository vehicleCheckRepository;
  final VehicleCheckMapper vehicleCheckMapper;
  final RentalRepository rentalRepository;
  final UserRepository userRepository;
  final FileStorageService fileStorageService;

  @Override
  public VehicleCheckResponse createVehicleCheck(String email, VehicleCheckRequest request) {
    // 1. Lấy staff từ email (đang đăng nhập)
    User staff = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    // 2. Lấy Rental
    Rental rental = rentalRepository.findById(request.getRentalId())
        .orElseThrow(() -> new NotFoundException("Rental not found with ID: " + request.getRentalId()));

    // 3. Tự động detect checkType dựa trên rental status
    String checkType = detectCheckType(rental);

    // 4. Xử lý upload ảnh
    String imageUrls = uploadImages(request.getImages());

    // 5. Tạo VehicleChecks
    VehicleChecks vehicleChecks = VehicleChecks.builder()
        .rental(rental)
        .staff(staff)
        .checkType(checkType)
        .notes(request.getNotes())
        .imageUrls(imageUrls)
        .build();

    VehicleChecks saved = vehicleCheckRepository.save(vehicleChecks);
    return vehicleCheckMapper.toVehicleCheckResponse(saved);
  }

  @Override
  public VehicleCheckResponse updateVehicleCheck(Long id, String email, VehicleCheckRequest request) {
    VehicleChecks existingVehicleCheck = vehicleCheckRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("VehicleCheck not found with ID: " + id));

    User staff = userRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

    Rental rental = rentalRepository.findById(request.getRentalId())
        .orElseThrow(() -> new NotFoundException("Rental not found with ID: " + request.getRentalId()));

    String checkType = detectCheckType(rental);
    String imageUrls = uploadImages(request.getImages());

    existingVehicleCheck.setStaff(staff);
    existingVehicleCheck.setRental(rental);
    existingVehicleCheck.setCheckType(checkType);
    existingVehicleCheck.setNotes(request.getNotes());
    if (imageUrls != null && !imageUrls.isEmpty()) {
      existingVehicleCheck.setImageUrls(imageUrls);
    }

    return vehicleCheckMapper.toVehicleCheckResponse(vehicleCheckRepository.save(existingVehicleCheck));
  }

  @Override
  public VehicleCheckResponse getVehicleCheckById(Long id) {
    return vehicleCheckRepository.findById(id)
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
  public void deleteVehicleCheck(Long id) {
    if (!vehicleCheckRepository.existsById(id)) {
      throw new NotFoundException("VehicleCheck not found with ID: " + id);
    }
    vehicleCheckRepository.deleteById(id);
  }

  // ========== HELPER METHODS ==========

  /**
   * Tự động detect checkType dựa trên rental status
   * - Nếu rental chưa bắt đầu (PENDING_PICKUP) -> PRE-RENTAL
   * - Nếu rental đang diễn ra (ACTIVE) -> PRE-RENTAL (đang kiểm tra lúc nhận xe)
   * - Nếu rental kết thúc (COMPLETED, CANCELLED) -> POST-RENTAL
   */
  private String detectCheckType(Rental rental) {
    if (rental == null || rental.getStatus() == null) {
      throw new BadRequestException("Invalid rental status");
    }

    RentalStatusEnum status = rental.getStatus();

    if (status == RentalStatusEnum.PENDING_PICKUP ||
        status == RentalStatusEnum.ACTIVE) {
      return "PRE-RENTAL"; // Kiểm tra trước khi khách lấy/đang lấy xe
    } else if (status == RentalStatusEnum.COMPLETED || status == RentalStatusEnum.CANCELLED) {
      return "POST-RENTAL"; // Kiểm tra sau khi khách trả xe
    }

    return "MAINTENANCE"; // Mặc định cho các trường hợp khác
  }

  /**
   * Upload ảnh và trả về comma-separated URLs
   */
  private String uploadImages(List<MultipartFile> images) {
    if (images == null || images.isEmpty()) {
      return null;
    }

    try {
      List<String> urls = new ArrayList<>();

      for (MultipartFile file : images) {
        if (!file.isEmpty()) {
          String url = fileStorageService.saveFile(file);
          urls.add(url);
        }
      }

      // Nối thành chuỗi comma-separated
      return String.join(",", urls);
    } catch (Exception e) {
      log.error("Error uploading images", e);
      throw new BadRequestException("Failed to upload images");
    }
  }
}
