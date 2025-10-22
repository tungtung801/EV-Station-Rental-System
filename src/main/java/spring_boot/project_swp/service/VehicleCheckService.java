package spring_boot.project_swp.service;

import spring_boot.project_swp.dto.request.VehicleCheckRequest;
import spring_boot.project_swp.dto.response.VehicleCheckResponse;

import java.util.List;

public interface VehicleCheckService {
    VehicleCheckResponse createVehicleCheck(VehicleCheckRequest request);
    VehicleCheckResponse getVehicleCheckById(Long id);
    List<VehicleCheckResponse> getAllVehicleChecks();
    VehicleCheckResponse updateVehicleCheck(Long id, VehicleCheckRequest request);
    void deleteVehicleCheck(Long id);
}