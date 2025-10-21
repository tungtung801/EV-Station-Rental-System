package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.VehicleModelRequest;
import spring_boot.project_swp.dto.response.VehicleModelResponse;

import java.util.List;

public interface VehicleModelService {
    public List<VehicleModelResponse> getAllVehicleModels();
    public VehicleModelResponse getVehicleModelById(int id);
    public VehicleModelResponse addVehicleModel(VehicleModelRequest request);
    public VehicleModelResponse updateVehicleModel(int id, VehicleModelRequest request);
    public void deleteVehicleModel(int id);
}
