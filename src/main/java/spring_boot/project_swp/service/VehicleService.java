package spring_boot.project_swp.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile; // Nếu muốn tách hàm upload ảnh
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;

public interface VehicleService {
    VehicleResponse addVehicle(VehicleRequest request);

    // Sửa tham số đầu vào cho đúng với Impl
    VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request);

    void deleteVehicle(Long vehicleId);

    List<VehicleResponse> findAll();

    VehicleResponse findById(Long vehicleId);

    // Nếu muốn tách hàm upload ảnh riêng thì thêm vào, còn ko thì thôi
    // String uploadImage(Long id, MultipartFile file);
    List<VehicleResponse> searchAvailableVehicles(Long stationId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}