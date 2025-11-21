package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;

public interface VehicleModelService {
  List<VehicleModelResponse> getAllVehicleModels();

  VehicleModelResponse getVehicleModelById(Long id);

  VehicleModelResponse addVehicleModel(VehicleModelRequest request);

  VehicleModelResponse updateVehicleModel(Long id, VehicleModelRequest request);

  void deleteVehicleModel(Long id);
}
