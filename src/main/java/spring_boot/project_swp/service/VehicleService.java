package spring_boot.project_swp.service;

import java.util.List;
import spring_boot.project_swp.dto.request.VehicleRequest;
import spring_boot.project_swp.dto.request.VehicleUpdateRequest;
import spring_boot.project_swp.dto.response.VehicleResponse;

public interface VehicleService {
  public VehicleResponse addVehicle(VehicleRequest request);

  public VehicleResponse updateVehicle(Long id, VehicleUpdateRequest request);

  public void deleteVehicle(Long vehicleId);

  public List<VehicleResponse> findAll();

  public VehicleResponse findById(Long vehicleId);
}
