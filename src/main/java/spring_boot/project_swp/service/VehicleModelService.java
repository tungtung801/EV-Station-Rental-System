package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;

public interface VehicleModelService {
  public List<VehicleModelResponse> getAllVehicleModels();

  public VehicleModelResponse getVehicleModelById(Long id);

  public VehicleModelResponse addVehicleModel(VehicleModelRequest request);

  public VehicleModelResponse updateVehicleModel(Long id, VehicleModelRequest request);

  public void deleteVehicleModel(Long id);
}
